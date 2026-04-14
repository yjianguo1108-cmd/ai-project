package com.grain.system.module.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.sales.entity.SalesOrder;
import com.grain.system.module.sales.vo.SalesOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {

    IPage<SalesOrderVO> selectOrderPage(Page<SalesOrderVO> page,
                                        @Param("keyword") String keyword,
                                        @Param("orgId") Integer orgId,
                                        @Param("grainId") Integer grainId,
                                        @Param("status") Integer status,
                                        @Param("paymentStatus") Integer paymentStatus);

    SalesOrderVO selectOrderById(@Param("id") Integer id);
}
