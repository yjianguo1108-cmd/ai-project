package com.grain.system.module.system.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.common.util.AesUtil;
import com.grain.system.module.system.dto.FarmerCreateDTO;
import com.grain.system.module.system.entity.Farmer;
import com.grain.system.module.system.entity.User;
import com.grain.system.module.system.mapper.FarmerMapper;
import com.grain.system.module.system.mapper.UserMapper;
import com.grain.system.module.system.service.FarmerService;
import com.grain.system.module.system.vo.FarmerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FarmerServiceImpl implements FarmerService {

    private final FarmerMapper farmerMapper;
    private final UserMapper userMapper;
    private final AesUtil aesUtil;

    @Override
    public IPage<FarmerVO> getFarmerPage(int page, int size, String keyword, Integer auditStatus) {
        Page<FarmerVO> pageParam = new Page<>(page, size);
        IPage<FarmerVO> result = farmerMapper.selectFarmerPage(pageParam, keyword, auditStatus);
        result.getRecords().forEach(vo -> {
            if (vo.getPhone() != null) {
                vo.setPhoneMask(AesUtil.maskPhone(vo.getPhone()));
                vo.setPhone(null);
            }
        });
        return result;
    }

    @Override
    public FarmerVO getFarmerById(Integer id) {
        Farmer farmer = farmerMapper.selectById(id);
        if (farmer == null) throw new BusinessException("农户档案不存在");
        User user = userMapper.selectById(farmer.getUserId());

        FarmerVO vo = new FarmerVO();
        vo.setId(farmer.getId());
        vo.setUserId(farmer.getUserId());
        if (user != null) {
            vo.setRealName(user.getRealName());
            vo.setPhoneMask(AesUtil.maskPhone(user.getPhone()));
        }
        vo.setIdCard(aesUtil.decrypt(farmer.getIdCard()));
        vo.setIdCardMask(farmer.getIdCardMask());
        vo.setBankCard(aesUtil.decrypt(farmer.getBankCard()));
        vo.setBankCardMask(farmer.getBankCardMask());
        vo.setBankName(farmer.getBankName());
        vo.setPreferredGrainTypes(farmer.getPreferredGrainTypes());
        vo.setAuditStatus(farmer.getAuditStatus());
        vo.setCreateTime(farmer.getCreateTime());
        return vo;
    }

    @Override
    public void createFarmer(FarmerCreateDTO dto, Integer operatorId) {
        // 检查用户是否已建档
        long count = farmerMapper.selectCount(
                new LambdaQueryWrapper<Farmer>().eq(Farmer::getUserId, dto.getUserId()));
        if (count > 0) throw new BusinessException("该用户已有农户档案");

        // 身份证唯一性校验（通过hash）
        if (dto.getIdCard() != null && !dto.getIdCard().isEmpty()) {
            String hash = DigestUtil.sha256Hex(dto.getIdCard());
            long hashCount = farmerMapper.selectCount(
                    new LambdaQueryWrapper<Farmer>().eq(Farmer::getIdCardHash, hash));
            if (hashCount > 0) throw new BusinessException("该身份证号已存在");
        }

        Farmer farmer = new Farmer();
        farmer.setUserId(dto.getUserId());
        if (dto.getIdCard() != null && !dto.getIdCard().isEmpty()) {
            farmer.setIdCard(aesUtil.encrypt(dto.getIdCard()));
            farmer.setIdCardMask(AesUtil.maskIdCard(dto.getIdCard()));
            farmer.setIdCardHash(DigestUtil.sha256Hex(dto.getIdCard()));
        }
        if (dto.getBankCard() != null && !dto.getBankCard().isEmpty()) {
            farmer.setBankCard(aesUtil.encrypt(dto.getBankCard()));
            farmer.setBankCardMask(AesUtil.maskBankCard(dto.getBankCard()));
        }
        farmer.setBankName(dto.getBankName());
        farmer.setPreferredGrainTypes(dto.getPreferredGrainTypes());
        farmer.setRemark(dto.getRemark());
        farmer.setAuditStatus(1);
        farmer.setCreateUserId(operatorId);
        farmerMapper.insert(farmer);
    }

    @Override
    public void updateFarmer(Integer id, FarmerCreateDTO dto, Integer operatorId) {
        Farmer farmer = farmerMapper.selectById(id);
        if (farmer == null) throw new BusinessException("农户档案不存在");

        if (dto.getIdCard() != null && !dto.getIdCard().isEmpty()) {
            String hash = DigestUtil.sha256Hex(dto.getIdCard());
            long hashCount = farmerMapper.selectCount(
                    new LambdaQueryWrapper<Farmer>().eq(Farmer::getIdCardHash, hash).ne(Farmer::getId, id));
            if (hashCount > 0) throw new BusinessException("该身份证号已存在");
            farmer.setIdCard(aesUtil.encrypt(dto.getIdCard()));
            farmer.setIdCardMask(AesUtil.maskIdCard(dto.getIdCard()));
            farmer.setIdCardHash(hash);
        }
        if (dto.getBankCard() != null && !dto.getBankCard().isEmpty()) {
            farmer.setBankCard(aesUtil.encrypt(dto.getBankCard()));
            farmer.setBankCardMask(AesUtil.maskBankCard(dto.getBankCard()));
        }
        if (dto.getUserId() != null && !dto.getUserId().equals(farmer.getUserId())) {
            long taken = farmerMapper.selectCount(
                    new LambdaQueryWrapper<Farmer>().eq(Farmer::getUserId, dto.getUserId()).ne(Farmer::getId, id));
            if (taken > 0) throw new BusinessException("该用户已有农户档案");
            farmer.setUserId(dto.getUserId());
        }
        if (dto.getBankName() != null) farmer.setBankName(dto.getBankName());
        if (dto.getPreferredGrainTypes() != null) farmer.setPreferredGrainTypes(dto.getPreferredGrainTypes());
        if (dto.getRemark() != null) farmer.setRemark(dto.getRemark());
        farmerMapper.updateById(farmer);
    }

    @Override
    public void updateAuditStatus(Integer id, Integer auditStatus, Integer operatorId) {
        Farmer farmer = farmerMapper.selectById(id);
        if (farmer == null) throw new BusinessException("农户档案不存在");
        farmer.setAuditStatus(auditStatus);
        farmerMapper.updateById(farmer);
    }

    @Override
    public void deleteFarmer(Integer id) {
        farmerMapper.deleteById(id);
    }
}
