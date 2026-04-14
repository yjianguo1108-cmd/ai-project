package com.grain.system.module.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.system.mapper.GrainMapper;
import com.grain.system.module.system.mapper.StoragePositionMapper;
import com.grain.system.module.warehouse.dto.CheckAuditDTO;
import com.grain.system.module.warehouse.dto.CheckDetailUpdateDTO;
import com.grain.system.module.warehouse.dto.CheckPlanCreateDTO;
import com.grain.system.module.warehouse.dto.OutboundCreateDTO;
import com.grain.system.module.warehouse.entity.Inventory;
import com.grain.system.module.warehouse.entity.InventoryCheckDetail;
import com.grain.system.module.warehouse.entity.InventoryCheckPlan;
import com.grain.system.module.warehouse.entity.InventoryLedger;
import com.grain.system.module.warehouse.entity.OutboundOrder;
import com.grain.system.module.warehouse.mapper.InventoryCheckDetailMapper;
import com.grain.system.module.warehouse.mapper.InventoryCheckPlanMapper;
import com.grain.system.module.warehouse.mapper.InventoryLedgerMapper;
import com.grain.system.module.warehouse.mapper.InventoryMapper;
import com.grain.system.module.warehouse.mapper.OutboundOrderMapper;
import com.grain.system.module.warehouse.service.InventoryCheckService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryCheckServiceImpl implements InventoryCheckService {

    private final InventoryCheckPlanMapper planMapper;
    private final InventoryCheckDetailMapper detailMapper;
    private final OutboundOrderMapper outboundMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryLedgerMapper ledgerMapper;
    private final GrainMapper grainMapper;
    private final StoragePositionMapper positionMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter CHECK_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final BigDecimal MAX_DEVIATION_RATE = new BigDecimal("0.03");

    @Override
    public IPage<InventoryCheckPlan> getPlanPage(int page, int size, Integer status, Integer checkType) {
        Page<InventoryCheckPlan> pageParam = new Page<>(page, size);
        return planMapper.selectPlanPage(pageParam, status, checkType);
    }

    @Override
    public InventoryCheckPlan getPlanById(Integer id) {
        return planMapper.selectPlanById(id);
    }

    @Override
    @Transactional
    public void createPlan(CheckPlanCreateDTO dto, Integer operatorId) {
        if (dto.getGrainIds().isEmpty() || dto.getPositionIds().isEmpty()) {
            throw new BusinessException("请选择粮食品种和储位");
        }

        InventoryCheckPlan plan = new InventoryCheckPlan();
        plan.setCheckNo(generateCheckNo());
        plan.setCheckType(dto.getCheckType());
        plan.setDeadline(dto.getDeadline());
        plan.setStatus(0);
        plan.setTotalItems(dto.getGrainIds().size() * dto.getPositionIds().size());
        plan.setCheckedItems(0);
        plan.setCreateUserId(operatorId);
        plan.setCreateTime(LocalDateTime.now());

        try {
            plan.setScopeGrainIds(objectMapper.writeValueAsString(dto.getGrainIds()));
            plan.setScopePositionIds(objectMapper.writeValueAsString(dto.getPositionIds()));
            plan.setOperatorIds(objectMapper.writeValueAsString(dto.getOperatorIds()));
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据序列化失败");
        }

        planMapper.insert(plan);
    }

    @Override
    @Transactional
    public void publishPlan(Integer id, Integer operatorId) {
        InventoryCheckPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BusinessException("盘点计划不存在");
        if (plan.getStatus() != 0) throw new BusinessException("只有草稿状态的计划可以发布");

        try {
            List<Integer> grainIds = objectMapper.readValue(plan.getScopeGrainIds(), List.class);
            List<Integer> positionIds = objectMapper.readValue(plan.getScopePositionIds(), List.class);

            for (Integer grainId : grainIds) {
                for (Integer positionId : positionIds) {
                    Inventory inventory = inventoryMapper.selectOne(
                        new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getGrainId, grainId)
                            .eq(Inventory::getPositionId, positionId)
                            .eq(Inventory::getStatus, 1)
                    );

                    InventoryCheckDetail detail = new InventoryCheckDetail();
                    detail.setCheckId(id);
                    detail.setGrainId(grainId);
                    detail.setPositionId(positionId);

                    if (inventory != null) {
                        detail.setInventoryId(inventory.getId());
                        detail.setBookQty(inventory.getCurrentStock());
                    } else {
                        detail.setBookQty(BigDecimal.ZERO);
                    }

                    detailMapper.insert(detail);
                }
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据解析失败");
        }

        plan.setStatus(1);
        planMapper.updateById(plan);
    }

    @Override
    public List<InventoryCheckDetail> getDetailList(Integer planId) {
        return detailMapper.selectDetailList(planId);
    }

    @Override
    @Transactional
    public void updateDetail(Integer id, CheckDetailUpdateDTO dto, Integer operatorId) {
        InventoryCheckDetail detail = detailMapper.selectById(id);
        if (detail == null) throw new BusinessException("盘点明细不存在");
        if (detail.getActualQty() != null) throw new BusinessException("该明细已录入");

        BigDecimal bookQty = detail.getBookQty();
        detail.setActualQty(dto.getActualQty());
        detail.setDiffQty(dto.getActualQty().subtract(bookQty));

        BigDecimal diffRate = BigDecimal.ZERO;
        if (bookQty.compareTo(BigDecimal.ZERO) > 0) {
            diffRate = detail.getDiffQty().divide(bookQty, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        }
        detail.setDiffRate(diffRate);

        try {
            List<String> photoUrls = dto.getPhotoUrls();
            detail.setPhotoUrls(photoUrls != null ? objectMapper.writeValueAsString(photoUrls) : null);
        } catch (JsonProcessingException e) {
            throw new BusinessException("照片数据序列化失败");
        }

        detail.setOperatorId(operatorId);
        detail.setCheckTime(LocalDateTime.now());
        detailMapper.updateById(detail);
    }

    @Override
    @Transactional
    public void submitPlan(Integer id, Integer operatorId) {
        InventoryCheckPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BusinessException("盘点计划不存在");
        if (plan.getStatus() != 1) throw new BusinessException("只有已发布的计划可以提交");

        List<InventoryCheckDetail> details = getDetailList(id);
        long unrecordedCount = details.stream().filter(d -> d.getActualQty() == null).count();
        if (unrecordedCount > 0) {
            throw new BusinessException("还有" + unrecordedCount + "条明细未录入实盘数量");
        }

        int checkedCount = details.size();
        plan.setCheckedItems(checkedCount);

        long diffCount = details.stream().filter(d -> d.getDiffQty() != null && d.getDiffQty().compareTo(BigDecimal.ZERO) != 0).count();

        BigDecimal matchRate = BigDecimal.valueOf(100);
        if (diffCount > 0) {
            matchRate = BigDecimal.valueOf(checkedCount - diffCount)
                .divide(BigDecimal.valueOf(checkedCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        plan.setMatchRate(matchRate);

        plan.setStatus(2);
        planMapper.updateById(plan);
    }

    @Override
    @Transactional
    public void auditPlan(Integer id, CheckAuditDTO dto, Integer operatorId) {
        InventoryCheckPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BusinessException("盘点计划不存在");
        if (plan.getStatus() != 2 && plan.getStatus() != 3) throw new BusinessException("只有待审核的计划可以审核");

        if ("pass".equals(dto.getResult())) {
            plan.setStatus(4);
            plan.setCompleteTime(LocalDateTime.now());

            for (CheckAuditDTO.DiffDetailDTO diff : dto.getDiffDetails()) {
                InventoryCheckDetail detail = detailMapper.selectById(diff.getDetailId());
                if (detail != null && "adjust".equals(diff.getHandleType())) {
                    Inventory inventory = inventoryMapper.selectOne(
                        new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getGrainId, detail.getGrainId())
                            .eq(Inventory::getPositionId, detail.getPositionId())
                            .eq(Inventory::getStatus, 1)
                    );
                    if (inventory != null) {
                        BigDecimal beforeStock = inventory.getCurrentStock();
                        inventory.setCurrentStock(detail.getActualQty());
                        inventory.setBookStock(detail.getActualQty());
                        inventoryMapper.updateById(inventory);

                        InventoryLedger ledger = new InventoryLedger();
                        ledger.setInventoryId(inventory.getId());
                        ledger.setLedgerType(2);
                        ledger.setChangeQty(detail.getDiffQty());
                        ledger.setBeforeQty(beforeStock);
                        ledger.setAfterQty(detail.getActualQty());
                        ledger.setRefBillType(2);
                        ledger.setRefBillId(plan.getId());
                        ledger.setOperatorId(operatorId);
                        ledger.setOperateTime(LocalDateTime.now());
                        ledger.setRemark(diff.getAdjustReason());
                        ledgerMapper.insert(ledger);

                        detail.setHandleType(0);
                        detail.setHandleReason(diff.getAdjustReason());
                        detail.setHandleUserId(operatorId);
                        detail.setHandleTime(LocalDateTime.now());
                        detailMapper.updateById(detail);
                    }
                }
            }
        } else if ("diff".equals(dto.getResult())) {
            plan.setStatus(3);
        }

        planMapper.updateById(plan);
    }

    @Override
    @Transactional
    public void handleDiff(Integer id, String handleType, String adjustReason, Integer operatorId) {
        InventoryCheckDetail detail = detailMapper.selectById(id);
        if (detail == null) throw new BusinessException("盘点明细不存在");

        detail.setHandleType("adjust".equals(handleType) ? 0 : 1);
        detail.setHandleReason(adjustReason);
        detail.setHandleUserId(operatorId);
        detail.setHandleTime(LocalDateTime.now());
        detailMapper.updateById(detail);
    }

    @Override
    public String getReportUrl(Integer id) {
        InventoryCheckPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BusinessException("盘点计划不存在");
        return plan.getReportUrl();
    }

    @Override
    public IPage<OutboundOrder> getOutboundPage(int page, int size, Integer grainId, Integer positionId, String dateFrom, String dateTo, Integer status) {
        Page<OutboundOrder> pageParam = new Page<>(page, size);
        return outboundMapper.selectOutboundPage(pageParam, grainId, positionId, dateFrom, dateTo, status);
    }

    @Override
    public OutboundOrder getOutboundById(Integer id) {
        return outboundMapper.selectOutboundById(id);
    }

    @Override
    @Transactional
    public void createOutbound(OutboundCreateDTO dto, Integer operatorId) {
        com.grain.system.module.system.entity.Grain grain = grainMapper.selectById(dto.getGrainId());
        if (grain == null) throw new BusinessException("粮食品种不存在");

        com.grain.system.module.system.entity.StoragePosition position = positionMapper.selectById(dto.getPositionId());
        if (position == null) throw new BusinessException("储位不存在");

        Inventory inventory = inventoryMapper.selectOne(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGrainId, dto.getGrainId())
                .eq(Inventory::getPositionId, dto.getPositionId())
                .eq(Inventory::getStatus, 1)
        );
        if (inventory == null) throw new BusinessException("库存记录不存在");

        OutboundOrder outbound = new OutboundOrder();
        outbound.setOutboundNo(generateOutboundNo());
        outbound.setGrainId(dto.getGrainId());
        outbound.setPositionId(dto.getPositionId());
        outbound.setPlanWeight(dto.getOutWeight());
        outbound.setOutboundType(2);
        outbound.setOutboundReason(dto.getOutboundReason());
        outbound.setStatus(0);
        outbound.setBatchNo(1);
        outbound.setCreateUserId(operatorId);
        outbound.setCreateTime(LocalDateTime.now());
        outboundMapper.insert(outbound);
    }

    @Override
    @Transactional
    public void confirmOutbound(Integer id, Integer operatorId) {
        OutboundOrder outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException("出库单不存在");
        if (outbound.getStatus() != 0) throw new BusinessException("该出库单已确认");

        Inventory inventory = inventoryMapper.selectOne(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGrainId, outbound.getGrainId())
                .eq(Inventory::getPositionId, outbound.getPositionId())
                .eq(Inventory::getStatus, 1)
        );
        if (inventory == null) throw new BusinessException("库存记录不存在");

        if (inventory.getCurrentStock().compareTo(outbound.getPlanWeight()) < 0) {
            throw new BusinessException("库存不足");
        }

        BigDecimal beforeStock = inventory.getCurrentStock();
        inventory.setCurrentStock(inventory.getCurrentStock().subtract(outbound.getPlanWeight()));
        inventoryMapper.updateById(inventory);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setInventoryId(inventory.getId());
        ledger.setLedgerType(1);
        ledger.setChangeQty(outbound.getPlanWeight().negate());
        ledger.setBeforeQty(beforeStock);
        ledger.setAfterQty(inventory.getCurrentStock());
        ledger.setRefBillType(1);
        ledger.setRefBillId(outbound.getId());
        ledger.setOperatorId(operatorId);
        ledger.setOperateTime(LocalDateTime.now());
        ledgerMapper.insert(ledger);

        outbound.setActualWeight(outbound.getPlanWeight());
        outbound.setStatus(2);
        outbound.setConfirmUserId(operatorId);
        outbound.setConfirmTime(LocalDateTime.now());

        if (beforeStock.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal deviation = outbound.getPlanWeight().divide(beforeStock, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
            outbound.setDeviationRate(deviation);
        }

        outboundMapper.updateById(outbound);
    }

    @Override
    @Transactional
    public void voidOutbound(Integer id, String reason, Integer operatorId) {
        OutboundOrder outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException("出库单不存在");
        if (outbound.getStatus() != 0 && outbound.getStatus() != 1) {
            throw new BusinessException("只有待确认或执行中的出库单可以作废");
        }

        outbound.setStatus(3);
        outbound.setOutboundReason(reason);
        outboundMapper.updateById(outbound);
    }

    private String generateCheckNo() {
        String dateStr = LocalDateTime.now().format(CHECK_NO_FORMATTER);
        String redisKey = "inventory:check:sequence:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) sequence = 1L;
        return String.format("CK%s%04d", dateStr, sequence);
    }

    private String generateOutboundNo() {
        String dateStr = LocalDateTime.now().format(CHECK_NO_FORMATTER);
        String redisKey = "inventory:outbound:sequence:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) sequence = 1L;
        return String.format("OUT%s%04d", dateStr, sequence);
    }
}
