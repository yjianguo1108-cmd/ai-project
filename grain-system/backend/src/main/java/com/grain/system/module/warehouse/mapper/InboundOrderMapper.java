package com.grain.system.module.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.warehouse.entity.InboundOrder;
import com.grain.system.module.warehouse.vo.InboundOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InboundOrderMapper extends BaseMapper<InboundOrder> {

    IPage<InboundOrderVO> selectInboundPage(Page<InboundOrderVO> page,
                                            @Param("keyword") String keyword,
                                            @Param("grainId") Integer grainId,
                                            @Param("positionId") Integer positionId);
}