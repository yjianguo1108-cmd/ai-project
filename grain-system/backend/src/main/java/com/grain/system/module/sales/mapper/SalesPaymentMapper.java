package com.grain.system.module.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.sales.entity.SalesPayment;
import com.grain.system.module.sales.vo.SalesPaymentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesPaymentMapper extends BaseMapper<SalesPayment> {

    IPage<SalesPaymentVO> selectPaymentPage(Page<SalesPaymentVO> page,
                                            @Param("orgId") Integer orgId,
                                            @Param("status") Integer status);

    SalesPaymentVO selectPaymentById(@Param("id") Integer id);
}
