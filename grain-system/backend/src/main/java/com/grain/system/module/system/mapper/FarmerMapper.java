package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.system.entity.Farmer;
import com.grain.system.module.system.vo.FarmerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FarmerMapper extends BaseMapper<Farmer> {

    IPage<FarmerVO> selectFarmerPage(Page<FarmerVO> page,
                                      @Param("keyword") String keyword,
                                      @Param("auditStatus") Integer auditStatus);
}
