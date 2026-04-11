package com.grain.system.module.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.warehouse.vo.InventoryLedgerVO;

public interface InventoryLedgerService {

    IPage<InventoryLedgerVO> getLedgerPage(int page, int size, Integer grainId, Integer storagePositionId);

    InventoryLedgerVO getLedgerById(Integer id);

    void adjustWeight(Integer id, java.math.BigDecimal adjustWeight, String reason, Integer operatorId);
}