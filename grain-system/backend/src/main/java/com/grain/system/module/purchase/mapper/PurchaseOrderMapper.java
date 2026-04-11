package com.grain.system.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.vo.PurchaseOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    IPage<PurchaseOrderVO> selectOrderPage(Page<PurchaseOrderVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("status") Integer status,
                                          @Param("farmerId") Integer farmerId,
                                          @Param("paymentStatus") Integer paymentStatus);
}