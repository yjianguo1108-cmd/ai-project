package com.grain.system.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.purchase.entity.WeighingRecord;
import com.grain.system.module.purchase.vo.WeighingRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeighingRecordMapper extends BaseMapper<WeighingRecord> {

    IPage<WeighingRecordVO> selectWeighingPage(Page<WeighingRecordVO> page,
                                                @Param("keyword") String keyword,
                                                @Param("status") Integer status,
                                                @Param("reserveId") Integer reserveId);
}