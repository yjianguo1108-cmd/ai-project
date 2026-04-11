package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.system.entity.DownstreamOrg;
import com.grain.system.module.system.vo.OrgVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrgMapper extends BaseMapper<DownstreamOrg> {

    IPage<OrgVO> selectOrgPage(Page<OrgVO> page,
                                @Param("keyword") String keyword,
                                @Param("auditStatus") Integer auditStatus,
                                @Param("orgType") Integer orgType);
}
