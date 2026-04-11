package com.grain.system.module.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.mapper.PurchaseOrderMapper;
import com.grain.system.module.warehouse.entity.InboundOrder;
import com.grain.system.module.warehouse.entity.Inventory;
import com.grain.system.module.warehouse.mapper.InboundOrderMapper;
import com.grain.system.module.warehouse.mapper.InventoryMapper;
import com.grain.system.module.warehouse.service.InboundOrderService;
import com.grain.system.module.warehouse.vo.InboundOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InboundOrderServiceImpl implements InboundOrderService {

    private final InboundOrderMapper inboundMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderMapper orderMapper;

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
        if (order.getStatus() != 2) throw new BusinessException("收购单状态不允许入库");

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
        long count = inboundMapper.selectCount(
                new LambdaQueryWrapper<InboundOrder>()
                        .likeLeft(InboundOrder::getInboundNo, "IN" + dateStr));
        return String.format("IN%s%04d", dateStr, count + 1);
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
        return vo;
    }
}