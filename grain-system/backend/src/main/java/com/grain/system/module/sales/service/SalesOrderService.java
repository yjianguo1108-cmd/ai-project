package com.grain.system.module.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.sales.dto.SalesOrderCreateDTO;
import com.grain.system.module.sales.dto.PaymentReceiveDTO;
import com.grain.system.module.sales.vo.SalesOrderVO;
import com.grain.system.module.sales.vo.SalesPaymentVO;

import java.math.BigDecimal;
import java.util.List;

public interface SalesOrderService {

    IPage<SalesOrderVO> getOrderPage(int page, int size, String keyword, Integer orgId, Integer grainId, Integer status, Integer paymentStatus);

    SalesOrderVO getOrderById(Integer id);

    void createOrder(SalesOrderCreateDTO dto, Integer operatorId);

    void submitOrder(Integer id, Integer operatorId);

    void auditOrder(Integer id, Integer status, String rejectReason, Integer operatorId);

    void voidOrder(Integer id, String reason, Integer operatorId);

    void startOutbound(Integer id, Integer operatorId);

    void confirmOutbound(Integer id, BigDecimal actualWeight, String attachmentUrl, Integer operatorId);

    void addOutboundForOrder(Integer orderId, BigDecimal planWeight, Integer operatorId);

    List<SalesOrderVO> getOutboundPage(int page, int size, Integer salesOrderId, Integer status);

    SalesOrderVO getOutboundById(Integer id);

    void voidOutbound(Integer id, String reason, Integer operatorId);

    IPage<SalesPaymentVO> getPaymentPage(int page, int size, Integer orgId, Integer status);

    SalesPaymentVO getPaymentById(Integer id);

    void receivePayment(Integer id, PaymentReceiveDTO dto, Integer operatorId);

    void addRemind(Integer orderId, String content, Integer operatorId);

    List<SalesOrderVO> getAvailableOrdersForOutbound();

    List<SalesOrderVO> getAvailableOrdersForPayment();
}
