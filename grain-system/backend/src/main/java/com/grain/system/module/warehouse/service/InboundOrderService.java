package com.grain.system.module.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.warehouse.vo.InboundOrderVO;

import java.util.List;

public interface InboundOrderService {

    IPage<InboundOrderVO> getInboundPage(int page, int size, String keyword, Integer grainId, Integer positionId);

    InboundOrderVO getInboundById(Integer id);

    void createInbound(Integer purchaseOrderId, Integer grainId, Integer positionId, java.math.BigDecimal weight, Integer operatorId);

    void confirmInbound(Integer id, Integer operatorId);

    void deleteInbound(Integer id);

    List<PurchaseOrder> getAvailableOrdersForInbound();

    void createInboundForPurchaseOrder(Integer purchaseOrderId, Integer positionId, Integer operatorId);
}