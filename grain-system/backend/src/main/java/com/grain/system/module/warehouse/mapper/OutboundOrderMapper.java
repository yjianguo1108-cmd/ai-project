package com.grain.system.module.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.warehouse.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {

    IPage<OutboundOrder> selectOutboundPage(Page<OutboundOrder> page,
                                            @Param("grainId") Integer grainId,
                                            @Param("positionId") Integer positionId,
                                            @Param("dateFrom") String dateFrom,
                                            @Param("dateTo") String dateTo,
                                            @Param("status") Integer status);

    OutboundOrder selectOutboundById(@Param("id") Integer id);
}
