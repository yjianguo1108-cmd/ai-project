package com.grain.system.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.system.entity.DownstreamOrg;
import com.grain.system.module.system.vo.OrgVO;

public interface OrgService {
    IPage<OrgVO> getOrgPage(int page, int size, String keyword, Integer auditStatus, Integer orgType);
    OrgVO getOrgById(Integer id);
    void createOrg(DownstreamOrg org, Integer operatorId);
    void updateOrg(Integer id, DownstreamOrg org, Integer operatorId);
    void audit(Integer id, Integer auditStatus, String rejectReason, Integer operatorId);
    void deleteOrg(Integer id);
}
