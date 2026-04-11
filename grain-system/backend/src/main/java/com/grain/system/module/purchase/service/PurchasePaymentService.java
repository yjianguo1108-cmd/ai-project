package com.grain.system.module.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.purchase.dto.PurchasePaymentCreateDTO;
import com.grain.system.module.purchase.vo.PurchasePaymentVO;

public interface PurchasePaymentService {

    IPage<PurchasePaymentVO> getPaymentPage(int page, int size, String keyword, Integer orderId);

    PurchasePaymentVO getPaymentById(Integer id);

    void createPayment(PurchasePaymentCreateDTO dto, Integer operatorId);

    void deletePayment(Integer id);
}