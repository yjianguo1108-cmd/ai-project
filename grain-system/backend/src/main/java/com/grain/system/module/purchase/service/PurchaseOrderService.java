package com.grain.system.module.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.purchase.dto.PurchaseOrderCreateDTO;
import com.grain.system.module.purchase.vo.PurchaseOrderVO;

public interface PurchaseOrderService {

    IPage<PurchaseOrderVO> getOrderPage(int page, int size, String keyword, Integer status, Integer farmerId, Integer paymentStatus);

    PurchaseOrderVO getOrderById(Integer id);

    void createOrder(PurchaseOrderCreateDTO dto, Integer operatorId);

    void updateOrder(Integer id, PurchaseOrderCreateDTO dto, Integer operatorId);

    void auditOrder(Integer id, Integer operatorId);

    void completeOrder(Integer id, Integer operatorId);

    void voidOrder(Integer id, String reason, Integer operatorId);

    void deleteOrder(Integer id);
}