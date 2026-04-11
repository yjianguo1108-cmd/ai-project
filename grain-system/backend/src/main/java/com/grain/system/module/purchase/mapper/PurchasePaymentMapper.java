package com.grain.system.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.purchase.entity.PurchasePayment;
import com.grain.system.module.purchase.vo.PurchasePaymentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchasePaymentMapper extends BaseMapper<PurchasePayment> {

    IPage<PurchasePaymentVO> selectPaymentPage(Page<PurchasePaymentVO> page,
                                               @Param("keyword") String keyword,
                                               @Param("orderId") Integer orderId);
}