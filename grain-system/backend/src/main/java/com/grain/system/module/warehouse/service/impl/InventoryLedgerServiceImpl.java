package com.grain.system.module.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.warehouse.entity.Inventory;
import com.grain.system.module.warehouse.mapper.InventoryLedgerMapper;
import com.grain.system.module.warehouse.service.InventoryLedgerService;
import com.grain.system.module.warehouse.vo.InventoryLedgerVO;
import com.grain.system.module.system.entity.Grain;
import com.grain.system.module.system.entity.StoragePosition;
import com.grain.system.module.system.mapper.GrainMapper;
import com.grain.system.module.system.mapper.StoragePositionMapper;
import com.grain.system.module.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InventoryLedgerServiceImpl implements InventoryLedgerService {

    private final InventoryLedgerMapper ledgerMapper;
    private final GrainMapper grainMapper;
    private final StoragePositionMapper positionMapper;
    private final UserMapper userMapper;

    @Override
    public IPage<InventoryLedgerVO> getLedgerPage(int page, int size, Integer grainId, Integer storagePositionId) {
        Page<InventoryLedgerVO> pageParam = new Page<>(page, size);
        return ledgerMapper.selectLedgerPage(pageParam, grainId, storagePositionId);
    }

    @Override
    public InventoryLedgerVO getLedgerById(Integer id) {
        Inventory inventory = ledgerMapper.selectById(id);
        if (inventory == null) throw new BusinessException("库存记录不存在");
        return convertToVO(inventory);
    }

    @Override
    @Transactional
    public void adjustWeight(Integer id, BigDecimal adjustWeight, String reason, Integer operatorId) {
        Inventory inventory = ledgerMapper.selectById(id);
        if (inventory == null) throw new BusinessException("库存记录不存在");

        BigDecimal newStock = inventory.getCurrentStock().add(adjustWeight);
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("调整后库存不能为负数");
        }

        inventory.setCurrentStock(newStock);
        inventory.setBookStock(newStock);
        ledgerMapper.updateById(inventory);
    }

    private InventoryLedgerVO convertToVO(Inventory inventory) {
        InventoryLedgerVO vo = new InventoryLedgerVO();
        vo.setId(inventory.getId());
        vo.setGrainId(inventory.getGrainId());
        vo.setStoragePositionId(inventory.getPositionId());
        vo.setCurrentWeight(inventory.getCurrentStock());
        vo.setCreateTime(inventory.getCreateTime());
        vo.setUpdateTime(inventory.getUpdateTime());

        if (inventory.getGrainId() != null) {
            Grain grain = grainMapper.selectById(inventory.getGrainId());
            if (grain != null) {
                vo.setGrainType(grain.getGrainType());
                vo.setGrainGrade(grain.getGrainGrade());
            }
        }

        if (inventory.getPositionId() != null) {
            StoragePosition position = positionMapper.selectById(inventory.getPositionId());
            if (position != null) {
                vo.setStoragePositionName(position.getPositionName());
            }
        }

        return vo;
    }
}