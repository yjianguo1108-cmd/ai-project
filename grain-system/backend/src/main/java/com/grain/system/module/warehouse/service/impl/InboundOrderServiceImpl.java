package com.grain.system.module.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.constant.RedisKey;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.mapper.PurchaseOrderMapper;
import com.grain.system.module.warehouse.entity.InboundOrder;
import com.grain.system.module.warehouse.entity.Inventory;
import com.grain.system.module.warehouse.mapper.InboundOrderMapper;
import com.grain.system.module.warehouse.mapper.InventoryMapper;
import com.grain.system.module.warehouse.service.InboundOrderService;
import com.grain.system.module.warehouse.vo.InboundOrderVO;
import com.grain.system.module.system.entity.Grain;
import com.grain.system.module.system.entity.StoragePosition;
import com.grain.system.module.system.mapper.GrainMapper;
import com.grain.system.module.system.mapper.StoragePositionMapper;
import com.grain.system.module.system.mapper.UserMapper;
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
public class InboundOrderServiceImpl implements InboundOrderService {

    private final InboundOrderMapper inboundMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderMapper orderMapper;
    private final GrainMapper grainMapper;
    private final StoragePositionMapper positionMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter INBOUND_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<InboundOrderVO> getInboundPage(int page, int size, String keyword, Integer grainId, Integer positionId) {
        Page<InboundOrderVO> pageParam = new Page<>(page, size);
        return inboundMapper.selectInboundPage(pageParam, keyword, grainId, positionId);
    }

    @Override
    public InboundOrderVO getInboundById(Integer id) {
        InboundOrder order = inboundMapper.selectById(id);
        if (order == null) throw new BusinessException("入库单不存在");
        return convertToVO(order);
    }

    @Override
    @Transactional
    public void createInbound(Integer purchaseOrderId, Integer grainId, Integer positionId, BigDecimal weight, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(purchaseOrderId);
        if (order == null) throw new BusinessException("收购单不存在");
        if (order.getStatus() != 3) throw new BusinessException("只有已完成的收购单可以入库");

        InboundOrder inbound = new InboundOrder();
        inbound.setInboundNo(generateInboundNo());
        inbound.setPurchaseOrderId(purchaseOrderId);
        inbound.setGrainId(grainId);
        inbound.setPositionId(positionId);
        inbound.setNetWeight(weight);
        inbound.setInboundType(0);
        inbound.setStatus(0);
        inbound.setCreateUserId(operatorId);
        inboundMapper.insert(inbound);
    }

    @Override
    public List<PurchaseOrder> getAvailableOrdersForInbound() {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 3)
               .orderByDesc(PurchaseOrder::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void createInboundForPurchaseOrder(Integer purchaseOrderId, Integer positionId, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(purchaseOrderId);
        if (order == null) throw new BusinessException("收购单据不存在");
        if (order.getStatus() != 3) throw new BusinessException("只有已完成的收购单据可以入库");

        Integer actualPositionId = positionId;
        if (actualPositionId == null) {
            LambdaQueryWrapper<StoragePosition> posWrapper = new LambdaQueryWrapper<>();
            posWrapper.eq(StoragePosition::getStatus, 1).last("LIMIT 1");
            StoragePosition defaultPosition = positionMapper.selectOne(posWrapper);
            if (defaultPosition == null) throw new BusinessException("没有可用的储位，请先配置储位");
            actualPositionId = defaultPosition.getId();
        }

        InboundOrder inbound = new InboundOrder();
        inbound.setInboundNo(generateInboundNo());
        inbound.setPurchaseOrderId(purchaseOrderId);
        inbound.setGrainId(order.getGrainId());
        inbound.setPositionId(actualPositionId);
        inbound.setNetWeight(order.getActualWeight());
        inbound.setInboundType(0);
        inbound.setStatus(0);
        inbound.setCreateUserId(operatorId);
        inboundMapper.insert(inbound);
    }

    @Override
    @Transactional
    public void confirmInbound(Integer id, Integer operatorId) {
        InboundOrder inbound = inboundMapper.selectById(id);
        if (inbound == null) throw new BusinessException("入库单不存在");
        if (inbound.getStatus() != 0) throw new BusinessException("当前状态不允许确认");

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getGrainId, inbound.getGrainId())
               .eq(Inventory::getPositionId, inbound.getPositionId());
        Inventory inventory = inventoryMapper.selectOne(wrapper);

        if (inventory == null) {
            inventory = new Inventory();
            inventory.setGrainId(inbound.getGrainId());
            inventory.setPositionId(inbound.getPositionId());
            inventory.setCurrentStock(inbound.getNetWeight());
            inventory.setBookStock(inbound.getNetWeight());
            inventory.setReservedStock(BigDecimal.ZERO);
            inventory.setEarliestInboundTime(LocalDateTime.now());
            inventory.setQualityStatus(0);
            inventory.setStatus(1);
            inventoryMapper.insert(inventory);
        } else {
            inventory.setCurrentStock(inventory.getCurrentStock().add(inbound.getNetWeight()));
            inventory.setBookStock(inventory.getBookStock().add(inbound.getNetWeight()));
            inventoryMapper.updateById(inventory);
        }

        inbound.setStatus(1);
        inbound.setConfirmUserId(operatorId);
        inbound.setConfirmTime(LocalDateTime.now());
        inboundMapper.updateById(inbound);
    }

    @Override
    @Transactional
    public void deleteInbound(Integer id) {
        InboundOrder inbound = inboundMapper.selectById(id);
        if (inbound == null) throw new BusinessException("入库单不存在");
        if (inbound.getStatus() != 0) throw new BusinessException("只有待确认的入库单可以删除");
        inboundMapper.deleteById(id);
    }

    private String generateInboundNo() {
        String dateStr = LocalDateTime.now().format(INBOUND_NO_FORMATTER);
        String redisKey = RedisKey.ORDER_PO_SEQUENCE + ":inbound:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) {
            sequence = 1L;
        }
        return String.format("IN%s%04d", dateStr, sequence);
    }

    private InboundOrderVO convertToVO(InboundOrder inbound) {
        InboundOrderVO vo = new InboundOrderVO();
        vo.setId(inbound.getId());
        vo.setInboundNo(inbound.getInboundNo());
        vo.setPurchaseOrderId(inbound.getPurchaseOrderId());
        vo.setGrainId(inbound.getGrainId());
        vo.setPositionId(inbound.getPositionId());
        vo.setNetWeight(inbound.getNetWeight());
        vo.setInboundType(inbound.getInboundType());
        vo.setInboundReason(inbound.getInboundReason());
        vo.setAttachmentUrl(inbound.getAttachmentUrl());
        vo.setStatus(inbound.getStatus());
        vo.setConfirmUserId(inbound.getConfirmUserId());
        vo.setConfirmTime(inbound.getConfirmTime());
        vo.setCreateUserId(inbound.getCreateUserId());
        vo.setCreateTime(inbound.getCreateTime());

        if (inbound.getPurchaseOrderId() != null) {
            PurchaseOrder order = orderMapper.selectById(inbound.getPurchaseOrderId());
            if (order != null) {
                vo.setPurchaseOrderNo(order.getOrderNo());
            }
        }

        if (inbound.getGrainId() != null) {
            Grain grain = grainMapper.selectById(inbound.getGrainId());
            if (grain != null) {
                vo.setGrainType(grain.getGrainType());
                vo.setGrainGrade(grain.getGrainGrade());
            }
        }

        if (inbound.getPositionId() != null) {
            StoragePosition position = positionMapper.selectById(inbound.getPositionId());
            if (position != null) {
                vo.setPositionName(position.getPositionName());
            }
        }

        return vo;
    }
}