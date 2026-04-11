package com.grain.system.module.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.purchase.dto.PurchaseReserveCreateDTO;
import com.grain.system.module.purchase.vo.PurchaseReserveVO;

public interface PurchaseReserveService {

    IPage<PurchaseReserveVO> getReservePage(int page, int size, String keyword, Integer status, Integer farmerId);

    PurchaseReserveVO getReserveById(Integer id);

    void createReserve(PurchaseReserveCreateDTO dto, Integer operatorId);

    void updateReserve(Integer id, PurchaseReserveCreateDTO dto, Integer operatorId);

    void auditReserve(Integer id, Integer auditStatus, String rejectReason, Integer operatorId);

    void scheduleReserve(Integer id, Integer operatorId);

    void cancelReserve(Integer id, String reason, Integer operatorId);

    void deleteReserve(Integer id);
}