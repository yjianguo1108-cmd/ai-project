package com.grain.system.module.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.warehouse.entity.InventoryCheckPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryCheckPlanMapper extends BaseMapper<InventoryCheckPlan> {

    IPage<InventoryCheckPlan> selectPlanPage(Page<InventoryCheckPlan> page,
                                            @Param("status") Integer status,
                                            @Param("checkType") Integer checkType);

    InventoryCheckPlan selectPlanById(@Param("id") Integer id);
}
