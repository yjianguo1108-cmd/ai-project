package com.grain.system.module.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.system.entity.DownstreamOrg;
import com.grain.system.module.system.mapper.OrgMapper;
import com.grain.system.module.system.service.OrgService;
import com.grain.system.module.system.vo.OrgVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final OrgMapper orgMapper;

    @Override
    public IPage<OrgVO> getOrgPage(int page, int size, String keyword, Integer auditStatus, Integer orgType) {
        return orgMapper.selectOrgPage(new Page<>(page, size), keyword, auditStatus, orgType);
    }

    @Override
    public OrgVO getOrgById(Integer id) {
        DownstreamOrg org = orgMapper.selectById(id);
        if (org == null) throw new BusinessException("机构不存在");
        return toVO(org);
    }

    @Override
    public void createOrg(DownstreamOrg org, Integer operatorId) {
        org.setAuditStatus(0);
        orgMapper.insert(org);
    }

    @Override
    public void updateOrg(Integer id, DownstreamOrg org, Integer operatorId) {
        DownstreamOrg existing = orgMapper.selectById(id);
        if (existing == null) throw new BusinessException("机构不存在");
        org.setId(id);
        // 审核通过后不允许修改，需重新提交审核
        if (existing.getAuditStatus() == 1) {
            existing.setAuditStatus(0); // 重置为待审核
            existing.setOrgName(org.getOrgName());
            existing.setOrgType(org.getOrgType());
            existing.setContactName(org.getContactName());
            existing.setPhone(org.getPhone());
            existing.setAddress(org.getAddress());
            orgMapper.updateById(existing);
        } else {
            orgMapper.updateById(org);
        }
    }

    @Override
    public void audit(Integer id, Integer auditStatus, String rejectReason, Integer operatorId) {
        DownstreamOrg org = orgMapper.selectById(id);
        if (org == null) throw new BusinessException("机构不存在");
        org.setAuditStatus(auditStatus);
        org.setAuditUserId(operatorId);
        org.setAuditTime(LocalDateTime.now());
        if (auditStatus == 2) {
            org.setRejectReason(rejectReason);
        }
        orgMapper.updateById(org);
    }

    @Override
    public void deleteOrg(Integer id) {
        orgMapper.deleteById(id);
    }

    private OrgVO toVO(DownstreamOrg org) {
        OrgVO vo = new OrgVO();
        vo.setId(org.getId());
        vo.setUserId(org.getUserId());
        vo.setOrgName(org.getOrgName());
        vo.setOrgType(org.getOrgType());
        vo.setContactName(org.getContactName());
        vo.setPhone(org.getPhone());
        vo.setAddress(org.getAddress());
        vo.setCredentialUrl(org.getCredentialUrl());
        vo.setAuditStatus(org.getAuditStatus());
        vo.setRejectReason(org.getRejectReason());
        vo.setAuditTime(org.getAuditTime());
        vo.setCreateTime(org.getCreateTime());
        return vo;
    }
}
