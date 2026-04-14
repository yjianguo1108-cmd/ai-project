package com.grain.system.module.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.sales.dto.PaymentReceiveDTO;
import com.grain.system.module.sales.dto.SalesOrderCreateDTO;
import com.grain.system.module.sales.entity.SalesOrder;
import com.grain.system.module.sales.entity.SalesPayment;
import com.grain.system.module.sales.entity.SalesPaymentRecord;
import com.grain.system.module.sales.mapper.SalesOrderMapper;
import com.grain.system.module.sales.mapper.SalesPaymentMapper;
import com.grain.system.module.sales.mapper.SalesPaymentRecordMapper;
import com.grain.system.module.sales.mapper.SalesCollectionRecordMapper;
import com.grain.system.module.sales.service.SalesOrderService;
import com.grain.system.module.sales.vo.SalesOrderVO;
import com.grain.system.module.sales.vo.SalesPaymentVO;
import com.grain.system.module.system.entity.DownstreamOrg;
import com.grain.system.module.system.entity.Grain;
import com.grain.system.module.system.entity.StoragePosition;
import com.grain.system.module.system.mapper.DownstreamOrgMapper;
import com.grain.system.module.system.mapper.GrainMapper;
import com.grain.system.module.system.mapper.StoragePositionMapper;
import com.grain.system.module.system.mapper.UserMapper;
import com.grain.system.module.warehouse.entity.Inventory;
import com.grain.system.module.warehouse.entity.OutboundOrder;
import com.grain.system.module.warehouse.entity.InventoryLedger;
import com.grain.system.module.warehouse.mapper.InventoryMapper;
import com.grain.system.module.warehouse.mapper.OutboundOrderMapper;
import com.grain.system.module.warehouse.mapper.InventoryLedgerRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderMapper orderMapper;
    private final SalesPaymentMapper paymentMapper;
    private final SalesPaymentRecordMapper recordMapper;
    private final SalesCollectionRecordMapper collectionRecordMapper;
    private final DownstreamOrgMapper orgMapper;
    private final GrainMapper grainMapper;
    private final StoragePositionMapper positionMapper;
    private final InventoryMapper inventoryMapper;
    private final OutboundOrderMapper outboundMapper;
    private final InventoryLedgerRecordMapper ledgerRecordMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter PAYMENT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter OUTBOUND_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<SalesOrderVO> getOrderPage(int page, int size, String keyword, Integer orgId, Integer grainId, Integer status, Integer paymentStatus) {
        Page<SalesOrderVO> pageParam = new Page<>(page, size);
        return orderMapper.selectOrderPage(pageParam, keyword, orgId, grainId, status, paymentStatus);
    }

    @Override
    public SalesOrderVO getOrderById(Integer id) {
        return orderMapper.selectOrderById(id);
    }

    @Override
    @Transactional
    public void createOrder(SalesOrderCreateDTO dto, Integer operatorId) {
        DownstreamOrg org = orgMapper.selectById(dto.getOrgId());
        if (org == null) throw new BusinessException("下游机构不存在");
        if (org.getAuditStatus() != 1) throw new BusinessException("下游机构未审核通过，无法创建销售订单");

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("订单明细不能为空");
        }

        for (SalesOrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            Grain grain = grainMapper.selectById(item.getGrainId());
            if (grain == null) throw new BusinessException("粮食品种不存在");

            StoragePosition position = positionMapper.selectById(item.getPositionId());
            if (position == null) throw new BusinessException("储位不存在");

            Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getGrainId, item.getGrainId())
                    .eq(Inventory::getPositionId, item.getPositionId())
                    .eq(Inventory::getStatus, 1)
            );
            if (inventory == null) throw new BusinessException("库存记录不存在");

            BigDecimal availableStock = inventory.getCurrentStock().subtract(inventory.getReservedStock() != null ? inventory.getReservedStock() : BigDecimal.ZERO);
            if (availableStock.compareTo(item.getPlanWeight()) < 0) {
                throw new BusinessException("储位" + position.getPositionName() + "库存不足，可用库存：" + availableStock);
            }

            BigDecimal minPrice = grain.getRefSalePrice().multiply(new BigDecimal("0.85"));
            BigDecimal maxPrice = grain.getRefSalePrice().multiply(new BigDecimal("1.30"));
            if (item.getActualPrice().compareTo(minPrice) < 0 || item.getActualPrice().compareTo(maxPrice) > 0) {
                throw new BusinessException("销售单价超出允许范围（参考价的85%~130%）");
            }
        }

        for (SalesOrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            SalesOrder order = new SalesOrder();
            order.setOrderNo(generateOrderNo());
            order.setOrgId(dto.getOrgId());
            order.setGrainId(item.getGrainId());
            order.setPositionId(item.getPositionId());
            order.setPlanWeight(item.getPlanWeight());
            order.setRefPrice(grainMapper.selectById(item.getGrainId()).getRefSalePrice());
            order.setActualPrice(item.getActualPrice());
            order.setTotalAmount(item.getPlanWeight().multiply(item.getActualPrice()));
            order.setActualOutWeight(BigDecimal.ZERO);
            order.setActualAmount(BigDecimal.ZERO);
            order.setReceivedAmount(BigDecimal.ZERO);
            order.setReservedStock(BigDecimal.ZERO);
            order.setStatus(0);
            order.setOutStatus(0);
            order.setPaymentStatus(0);
            order.setCreateUserId(operatorId);
            orderMapper.insert(order);
        }
    }

    @Override
    @Transactional
    public void submitOrder(Integer id, Integer operatorId) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("只有草稿状态的订单可以提交");
        order.setStatus(1);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void auditOrder(Integer id, Integer status, String rejectReason, Integer operatorId) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 1) throw new BusinessException("只有待审核的订单可以审核");

        if (status == 1) {
            order.setStatus(2);
        } else if (status == 6) {
            order.setStatus(6);
            order.setVoidReason(rejectReason);
        } else {
            throw new BusinessException("审核状态无效");
        }

        order.setAuditUserId(operatorId);
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        if (status == 1) {
            Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getGrainId, order.getGrainId())
                    .eq(Inventory::getPositionId, order.getPositionId())
                    .eq(Inventory::getStatus, 1)
            );
            if (inventory != null) {
                BigDecimal reservedStock = inventory.getReservedStock() != null ? inventory.getReservedStock() : BigDecimal.ZERO;
                inventory.setReservedStock(reservedStock.add(order.getPlanWeight()));
                inventoryMapper.updateById(inventory);
            }

            OutboundOrder outbound = new OutboundOrder();
            outbound.setOutboundNo(generateOutboundNo());
            outbound.setSalesOrderId(order.getId());
            outbound.setGrainId(order.getGrainId());
            outbound.setPositionId(order.getPositionId());
            outbound.setPlanWeight(order.getPlanWeight());
            outbound.setOutboundType(0);
            outbound.setStatus(0);
            outbound.setCreateUserId(operatorId);
            outboundMapper.insert(outbound);
        }
    }

    @Override
    @Transactional
    public void voidOrder(Integer id, String reason, Integer operatorId) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() == 5 || order.getStatus() == 7) {
            throw new BusinessException("当前状态不允许作废");
        }

        order.setStatus(7);
        order.setVoidReason(reason);
        orderMapper.updateById(order);

        if (order.getReservedStock() != null && order.getReservedStock().compareTo(BigDecimal.ZERO) > 0) {
            Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getGrainId, order.getGrainId())
                    .eq(Inventory::getPositionId, order.getPositionId())
                    .eq(Inventory::getStatus, 1)
            );
            if (inventory != null) {
                BigDecimal reservedStock = inventory.getReservedStock() != null ? inventory.getReservedStock() : BigDecimal.ZERO;
                inventory.setReservedStock(reservedStock.subtract(order.getReservedStock()));
                inventoryMapper.updateById(inventory);
            }
        }
    }

    @Override
    @Transactional
    public void startOutbound(Integer id, Integer operatorId) {
        OutboundOrder outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException("出库单不存在");
        if (outbound.getStatus() != 0) throw new BusinessException("当前状态不允许开始出库");

        outbound.setStatus(1);
        outboundMapper.updateById(outbound);

        Inventory inventory = inventoryMapper.selectOne(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGrainId, outbound.getGrainId())
                .eq(Inventory::getPositionId, outbound.getPositionId())
                .eq(Inventory::getStatus, 1)
        );
        if (inventory != null) {
            inventory.setStatus(4);
            inventoryMapper.updateById(inventory);
        }
    }

    @Override
    @Transactional
    public void confirmOutbound(Integer id, BigDecimal actualWeight, String attachmentUrl, Integer operatorId) {
        OutboundOrder outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException("出库单不存在");
        if (outbound.getStatus() != 1) throw new BusinessException("当前状态不允许确认出库");

        SalesOrder order = orderMapper.selectById(outbound.getSalesOrderId());
        if (order == null) throw new BusinessException("关联订单不存在");

        Inventory inventory = inventoryMapper.selectOne(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGrainId, outbound.getGrainId())
                .eq(Inventory::getPositionId, outbound.getPositionId())
                .eq(Inventory::getStatus, 4)
        );
        if (inventory == null) {
            inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getGrainId, outbound.getGrainId())
                    .eq(Inventory::getPositionId, outbound.getPositionId())
                    .eq(Inventory::getStatus, 1)
            );
        }

        if (inventory != null) {
            inventory.setCurrentStock(inventory.getCurrentStock().subtract(actualWeight));
            inventory.setReservedStock(inventory.getReservedStock() != null ? inventory.getReservedStock().subtract(order.getPlanWeight()) : BigDecimal.ZERO.subtract(order.getPlanWeight()));
            if (inventory.getCurrentStock().compareTo(BigDecimal.ZERO) < 0) {
                inventory.setCurrentStock(BigDecimal.ZERO);
            }
            inventory.setStatus(1);
            inventoryMapper.updateById(inventory);

            InventoryLedger ledger = new InventoryLedger();
            ledger.setInventoryId(inventory.getId());
            ledger.setLedgerType(1);
            ledger.setChangeQty(actualWeight.negate());
            ledger.setBeforeQty(inventory.getCurrentStock().add(actualWeight));
            ledger.setAfterQty(inventory.getCurrentStock());
            ledger.setRefBillType(1);
            ledger.setRefBillId(outbound.getId());
            ledger.setOperatorId(operatorId);
            ledger.setOperateTime(LocalDateTime.now());
            ledgerRecordMapper.insert(ledger);
        }

        outbound.setActualWeight(actualWeight);
        outbound.setAttachmentUrl(attachmentUrl);
        outbound.setStatus(2);
        outbound.setConfirmUserId(operatorId);
        outbound.setConfirmTime(LocalDateTime.now());
        outboundMapper.updateById(outbound);

        order.setActualOutWeight(order.getActualOutWeight().add(actualWeight));
        BigDecimal remaining = order.getPlanWeight().subtract(order.getActualOutWeight());
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            order.setOutStatus(2);
            order.setStatus(4);
        } else {
            order.setOutStatus(1);
        }
        orderMapper.updateById(order);

        if (order.getOutStatus() == 2) {
            SalesPayment payment = new SalesPayment();
            payment.setPaymentNo(generatePaymentNo());
            payment.setSalesOrderId(order.getId());
            payment.setOrgId(order.getOrgId());
            payment.setTotalReceivable(order.getActualOutWeight().multiply(order.getActualPrice()));
            payment.setReceivedAmount(BigDecimal.ZERO);
            payment.setStatus(0);
            paymentMapper.insert(payment);
        }
    }

    @Override
    @Transactional
    public void addOutboundForOrder(Integer orderId, BigDecimal planWeight, Integer operatorId) {
        SalesOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        OutboundOrder outbound = new OutboundOrder();
        outbound.setOutboundNo(generateOutboundNo());
        outbound.setSalesOrderId(order.getId());
        outbound.setGrainId(order.getGrainId());
        outbound.setPositionId(order.getPositionId());
        outbound.setPlanWeight(planWeight);
        outbound.setOutboundType(0);
        outbound.setStatus(0);
        outbound.setCreateUserId(operatorId);
        outboundMapper.insert(outbound);
    }

    @Override
    public IPage<SalesPaymentVO> getPaymentPage(int page, int size, Integer orgId, Integer status) {
        Page<SalesPaymentVO> pageParam = new Page<>(page, size);
        return paymentMapper.selectPaymentPage(pageParam, orgId, status);
    }

    @Override
    public SalesPaymentVO getPaymentById(Integer id) {
        return paymentMapper.selectPaymentById(id);
    }

    @Override
    @Transactional
    public void receivePayment(Integer id, PaymentReceiveDTO dto, Integer operatorId) {
        SalesPayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException("收款单不存在");
        if (payment.getStatus() == 3) throw new BusinessException("收款单已锁定，无法操作");
        if (payment.getStatus() == 2) throw new BusinessException("收款单已收款完成");

        SalesOrder order = orderMapper.selectById(payment.getSalesOrderId());
        if (order == null) throw new BusinessException("关联订单不存在");

        SalesPaymentRecord record = new SalesPaymentRecord();
        record.setPaymentId(payment.getId());
        record.setSalesOrderId(order.getId());
        record.setOrgId(order.getOrgId());
        record.setReceiveAmount(dto.getReceiveAmount());
        record.setReceiveMethod(dto.getReceiveMethod());
        record.setReceiveTime(dto.getReceiveTime() != null ? dto.getReceiveTime() : LocalDateTime.now());
        record.setFlowNo(dto.getFlowNo());
        record.setBankInfo(dto.getBankInfo());
        record.setRemark(dto.getRemark());
        record.setOperatorId(operatorId);
        recordMapper.insert(record);

        payment.setReceivedAmount(payment.getReceivedAmount().add(dto.getReceiveAmount()));

        BigDecimal tolerance = new BigDecimal("1.00");
        if (payment.getReceivedAmount().add(tolerance).compareTo(payment.getTotalReceivable()) >= 0) {
            payment.setStatus(2);
            order.setPaymentStatus(2);
            order.setStatus(5);
        } else {
            payment.setStatus(1);
            order.setPaymentStatus(1);
        }
        paymentMapper.updateById(payment);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void addRemind(Integer orderId, String content, Integer operatorId) {
        SalesOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        com.grain.system.module.sales.entity.SalesCollectionRecord remind = new com.grain.system.module.sales.entity.SalesCollectionRecord();
        remind.setSalesOrderId(orderId);
        remind.setOrgId(order.getOrgId());
        remind.setRemindContent(content);
        remind.setRemindTime(LocalDateTime.now());
        remind.setOperatorId(operatorId);
        remind.setCreateTime(LocalDateTime.now());
        collectionRecordMapper.insert(remind);
    }

    @Override
    public List<SalesOrderVO> getOutboundPage(int page, int size, Integer salesOrderId, Integer status) {
        Page<OutboundOrder> pageParam = new Page<>(page, size);
        IPage<OutboundOrder> result = outboundMapper.selectOutboundPage(pageParam, null, null, null, null, status);
        return result.getRecords().stream().map(out -> convertOutboundToVO(out)).collect(Collectors.toList());
    }

    @Override
    public SalesOrderVO getOutboundById(Integer id) {
        OutboundOrder outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException("出库单不存在");
        return convertOutboundToVO(outbound);
    }

    private SalesOrderVO convertOutboundToVO(OutboundOrder outbound) {
        SalesOrderVO vo = new SalesOrderVO();
        vo.setId(outbound.getId());
        vo.setOutboundNo(outbound.getOutboundNo());
        vo.setSalesOrderId(outbound.getSalesOrderId());
        vo.setGrainId(outbound.getGrainId());
        vo.setPositionId(outbound.getPositionId());
        vo.setPlanWeight(outbound.getPlanWeight());
        vo.setActualOutWeight(outbound.getActualWeight());
        vo.setOutStatus(outbound.getStatus());
        vo.setStatusName(getOutboundStatusName(outbound.getStatus()));
        vo.setAttachmentUrl(outbound.getAttachmentUrl());
        vo.setCreateTime(outbound.getCreateTime());

        if (outbound.getSalesOrderId() != null) {
            SalesOrder order = orderMapper.selectById(outbound.getSalesOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setOrgId(order.getOrgId());
                vo.setOrgName(null);
                vo.setGrainType(null);
                vo.setGrainGrade(null);
                vo.setActualPrice(order.getActualPrice());
                vo.setTotalAmount(order.getActualOutWeight().multiply(order.getActualPrice()));
            }
        }

        if (outbound.getGrainId() != null) {
            Grain grain = grainMapper.selectById(outbound.getGrainId());
            if (grain != null) {
                vo.setGrainType(grain.getGrainType());
                vo.setGrainGrade(grain.getGrainGrade());
            }
        }

        if (outbound.getPositionId() != null) {
            StoragePosition position = positionMapper.selectById(outbound.getPositionId());
            if (position != null) {
                vo.setPositionName(position.getPositionName());
            }
        }

        return vo;
    }

    private String getOutboundStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待出库";
            case 1: return "执行中";
            case 2: return "已完成";
            case 3: return "已作废";
            default: return "未知";
        }
    }

    @Override
    @Transactional
    public void voidOutbound(Integer id, String reason, Integer operatorId) {
        OutboundOrder outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException("出库单不存在");
        if (outbound.getStatus() == 2) throw new BusinessException("已完成状态不允许作废");

        outbound.setStatus(3);
        outboundMapper.updateById(outbound);
    }

    @Override
    public List<SalesOrderVO> getAvailableOrdersForOutbound() {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getStatus, 2)
               .orderByDesc(SalesOrder::getCreateTime);
        List<SalesOrder> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(order -> orderMapper.selectOrderById(order.getId())).collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderVO> getAvailableOrdersForPayment() {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getStatus, 4)
               .in(SalesOrder::getPaymentStatus, 0, 1)
               .orderByDesc(SalesOrder::getCreateTime);
        List<SalesOrder> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(order -> orderMapper.selectOrderById(order.getId())).collect(Collectors.toList());
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(ORDER_NO_FORMATTER);
        String redisKey = "order:sales:sequence:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) sequence = 1L;
        return String.format("SO%s%04d", dateStr, sequence);
    }

    private String generatePaymentNo() {
        String dateStr = LocalDateTime.now().format(PAYMENT_NO_FORMATTER);
        String redisKey = "order:payment:sales:sequence:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) sequence = 1L;
        return String.format("SP%s%04d", dateStr, sequence);
    }

    private String generateOutboundNo() {
        String dateStr = LocalDateTime.now().format(OUTBOUND_NO_FORMATTER);
        String redisKey = "order:outbound:sequence:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) sequence = 1L;
        return String.format("OUT%s%04d", dateStr, sequence);
    }
}
