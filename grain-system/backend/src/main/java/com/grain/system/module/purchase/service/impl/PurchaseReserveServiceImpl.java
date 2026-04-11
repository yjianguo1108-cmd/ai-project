package com.grain.system.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.dto.PurchaseReserveCreateDTO;
import com.grain.system.module.purchase.entity.PurchaseReserve;
import com.grain.system.module.purchase.mapper.PurchaseReserveMapper;
import com.grain.system.module.purchase.service.PurchaseReserveService;
import com.grain.system.module.purchase.vo.PurchaseReserveVO;
import com.grain.system.module.system.entity.Farmer;
import com.grain.system.module.system.mapper.FarmerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseReserveServiceImpl implements PurchaseReserveService {

    private final PurchaseReserveMapper reserveMapper;
    private final FarmerMapper farmerMapper;

    private static final DateTimeFormatter RESERVE_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<PurchaseReserveVO> getReservePage(int page, int size, String keyword, Integer status, Integer farmerId) {
        Page<PurchaseReserveVO> pageParam = new Page<>(page, size);
        return reserveMapper.selectReservePage(pageParam, keyword, status, farmerId);
    }

    @Override
    public PurchaseReserveVO getReserveById(Integer id) {
        PurchaseReserve reserve = reserveMapper.selectById(id);
        if (reserve == null) throw new BusinessException("预约单不存在");
        return convertToVO(reserve);
    }

    @Override
    @Transactional
    public void createReserve(PurchaseReserveCreateDTO dto, Integer operatorId) {
        Farmer farmer = farmerMapper.selectById(dto.getFarmerId());
        if (farmer == null) throw new BusinessException("农户档案不存在");
        if (farmer.getAuditStatus() != 1) throw new BusinessException("农户档案未审核通过，无法创建预约");

        PurchaseReserve reserve = new PurchaseReserve();
        reserve.setReserveNo(generateReserveNo());
        reserve.setFarmerId(dto.getFarmerId());
        reserve.setGrainId(dto.getGrainId());
        reserve.setEstimatedWeight(dto.getEstimatedWeight());
        reserve.setReserveTime(dto.getReserveTime());
        reserve.setStatus(0);
        reserve.setCreateUserId(operatorId);
        reserveMapper.insert(reserve);
    }

    @Override
    @Transactional
    public void updateReserve(Integer id, PurchaseReserveCreateDTO dto, Integer operatorId) {
        PurchaseReserve reserve = reserveMapper.selectById(id);
        if (reserve == null) throw new BusinessException("预约单不存在");
        if (reserve.getStatus() != 0 && reserve.getStatus() != 3) {
            throw new BusinessException("当前状态不允许修改");
        }

        reserve.setGrainId(dto.getGrainId());
        reserve.setEstimatedWeight(dto.getEstimatedWeight());
        reserve.setReserveTime(dto.getReserveTime());
        if (reserve.getStatus() == 3) {
            reserve.setStatus(0);
        }
        reserveMapper.updateById(reserve);
    }

    @Override
    @Transactional
    public void auditReserve(Integer id, Integer auditStatus, String rejectReason, Integer operatorId) {
        PurchaseReserve reserve = reserveMapper.selectById(id);
        if (reserve == null) throw new BusinessException("预约单不存在");
        if (reserve.getStatus() != 0) throw new BusinessException("当前状态不允许审核");

        if (auditStatus == 1) {
            reserve.setStatus(1);
        } else if (auditStatus == 3) {
            reserve.setStatus(3);
            reserve.setRejectReason(rejectReason);
        } else {
            throw new BusinessException("无效的审核状态");
        }
        reserve.setAuditUserId(operatorId);
        reserve.setAuditTime(LocalDateTime.now());
        reserveMapper.updateById(reserve);
    }

    @Override
    @Transactional
    public void scheduleReserve(Integer id, Integer operatorId) {
        PurchaseReserve reserve = reserveMapper.selectById(id);
        if (reserve == null) throw new BusinessException("预约单不存在");
        if (reserve.getStatus() != 1) throw new BusinessException("只有已审核的预约单才能排期");
        reserve.setStatus(2);
        reserveMapper.updateById(reserve);
    }

    @Override
    @Transactional
    public void cancelReserve(Integer id, String reason, Integer operatorId) {
        PurchaseReserve reserve = reserveMapper.selectById(id);
        if (reserve == null) throw new BusinessException("预约单不存在");
        if (reserve.getStatus() == 4) throw new BusinessException("已完成的预约单无法取消");
        reserve.setStatus(3);
        reserve.setRejectReason(reason);
        reserveMapper.updateById(reserve);
    }

    @Override
    @Transactional
    public void deleteReserve(Integer id) {
        PurchaseReserve reserve = reserveMapper.selectById(id);
        if (reserve == null) throw new BusinessException("预约单不存在");
        if (reserve.getStatus() != 0 && reserve.getStatus() != 3) {
            throw new BusinessException("只有待审核或已取消的预约单可以删除");
        }
        reserveMapper.deleteById(id);
    }

    private String generateReserveNo() {
        String dateStr = LocalDateTime.now().format(RESERVE_NO_FORMATTER);
        long count = reserveMapper.selectCount(
                new LambdaQueryWrapper<PurchaseReserve>()
                        .likeLeft(PurchaseReserve::getReserveNo, "RV" + dateStr));
        return String.format("RV%s%04d", dateStr, count + 1);
    }

    private PurchaseReserveVO convertToVO(PurchaseReserve reserve) {
        PurchaseReserveVO vo = new PurchaseReserveVO();
        vo.setId(reserve.getId());
        vo.setReserveNo(reserve.getReserveNo());
        vo.setFarmerId(reserve.getFarmerId());
        vo.setGrainId(reserve.getGrainId());
        vo.setEstimatedWeight(reserve.getEstimatedWeight());
        vo.setReserveTime(reserve.getReserveTime());
        vo.setStatus(reserve.getStatus());
        vo.setRejectReason(reserve.getRejectReason());
        vo.setAuditUserId(reserve.getAuditUserId());
        vo.setAuditTime(reserve.getAuditTime());
        vo.setCreateUserId(reserve.getCreateUserId());
        vo.setCreateTime(reserve.getCreateTime());
        vo.setUpdateTime(reserve.getUpdateTime());
        return vo;
    }
}