package com.grain.system.module.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.warehouse.entity.Inventory;
import com.grain.system.module.warehouse.vo.InventoryLedgerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryLedgerMapper extends BaseMapper<Inventory> {

    IPage<InventoryLedgerVO> selectLedgerPage(Page<InventoryLedgerVO> page,
                                              @Param("grainId") Integer grainId,
                                              @Param("storagePositionId") Integer storagePositionId);
}