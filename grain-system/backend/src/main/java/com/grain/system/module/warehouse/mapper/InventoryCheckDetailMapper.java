package com.grain.system.module.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.system.module.warehouse.entity.InventoryCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryCheckDetailMapper extends BaseMapper<InventoryCheckDetail> {

    List<InventoryCheckDetail> selectDetailList(@Param("checkId") Integer checkId);
}
