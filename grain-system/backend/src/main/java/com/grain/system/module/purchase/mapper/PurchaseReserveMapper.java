package com.grain.system.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.purchase.entity.PurchaseReserve;
import com.grain.system.module.purchase.vo.PurchaseReserveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseReserveMapper extends BaseMapper<PurchaseReserve> {

    IPage<PurchaseReserveVO> selectReservePage(Page<PurchaseReserveVO> page,
                                                @Param("keyword") String keyword,
                                                @Param("status") Integer status,
                                                @Param("farmerId") Integer farmerId);
}