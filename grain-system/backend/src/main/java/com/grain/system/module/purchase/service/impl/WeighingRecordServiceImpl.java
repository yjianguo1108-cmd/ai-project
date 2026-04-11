package com.grain.system.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.dto.WeighingRecordCreateDTO;
import com.grain.system.module.purchase.entity.WeighingRecord;
import com.grain.system.module.purchase.mapper.WeighingRecordMapper;
import com.grain.system.module.purchase.service.WeighingRecordService;
import com.grain.system.module.purchase.vo.WeighingRecordVO;
import com.grain.system.module.purchase.entity.PurchaseReserve;
import com.grain.system.module.purchase.mapper.PurchaseReserveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class WeighingRecordServiceImpl implements WeighingRecordService {

    private final WeighingRecordMapper weighingMapper;
    private final PurchaseReserveMapper reserveMapper;

    private static final DateTimeFormatter WEIGH_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<WeighingRecordVO> getWeighingPage(int page, int size, String keyword, Integer status, Integer reserveId) {
        Page<WeighingRecordVO> pageParam = new Page<>(page, size);
        return weighingMapper.selectWeighingPage(pageParam, keyword, status, reserveId);
    }

    @Override
    public WeighingRecordVO getWeighingById(Integer id) {
        WeighingRecord record = weighingMapper.selectById(id);
        if (record == null) throw new BusinessException("称重记录不存在");
        return convertToVO(record);
    }

    @Override
    @Transactional
    public void createWeighing(WeighingRecordCreateDTO dto, Integer operatorId) {
        if (dto.getReserveId() != null) {
            PurchaseReserve reserve = reserveMapper.selectById(dto.getReserveId());
            if (reserve == null) throw new BusinessException("预约单不存在");
            if (reserve.getStatus() != 2) throw new BusinessException("只有已排期的预约才能称重");
        }

        BigDecimal netWeight = dto.getGrossWeight().subtract(dto.getTareWeight());
        if (netWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("净重必须大于0");
        }

        WeighingRecord record = new WeighingRecord();
        record.setWeighNo(generateWeighNo());
        record.setReserveId(dto.getReserveId());
        record.setWeighbridgeId(dto.getWeighbridgeId());
        record.setGrossWeight(dto.getGrossWeight());
        record.setTareWeight(dto.getTareWeight());
        record.setNetWeight(netWeight);
        record.setWeighTime(dto.getWeighTime());
        record.setDataSource(dto.getDataSource());
        record.setRemark(dto.getRemark());
        record.setStatus(0);
        record.setCreateUserId(operatorId);
        weighingMapper.insert(record);
    }

    @Override
    @Transactional
    public void updateWeighing(Integer id, WeighingRecordCreateDTO dto, Integer operatorId) {
        WeighingRecord record = weighingMapper.selectById(id);
        if (record == null) throw new BusinessException("称重记录不存在");
        if (record.getStatus() != 0) throw new BusinessException("当前状态不允许修改");

        BigDecimal netWeight = dto.getGrossWeight().subtract(dto.getTareWeight());
        if (netWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("净重必须大于0");
        }

        record.setWeighbridgeId(dto.getWeighbridgeId());
        record.setGrossWeight(dto.getGrossWeight());
        record.setTareWeight(dto.getTareWeight());
        record.setNetWeight(netWeight);
        record.setWeighTime(dto.getWeighTime());
        record.setDataSource(dto.getDataSource());
        record.setRemark(dto.getRemark());
        weighingMapper.updateById(record);
    }

    @Override
    @Transactional
    public void auditWeighing(Integer id, Integer operatorId) {
        WeighingRecord record = weighingMapper.selectById(id);
        if (record == null) throw new BusinessException("称重记录不存在");
        if (record.getStatus() != 0) throw new BusinessException("当前状态不允许审核");

        record.setStatus(1);
        record.setAuditUserId(operatorId);
        record.setAuditTime(LocalDateTime.now());
        weighingMapper.updateById(record);
    }

    @Override
    @Transactional
    public void voidWeighing(Integer id, String reason, Integer operatorId) {
        WeighingRecord record = weighingMapper.selectById(id);
        if (record == null) throw new BusinessException("称重记录不存在");
        if (record.getStatus() == 2) throw new BusinessException("该记录已作废");

        record.setStatus(2);
        record.setRemark(record.getRemark() + " [作废原因:" + reason + "]");
        weighingMapper.updateById(record);
    }

    @Override
    @Transactional
    public void deleteWeighing(Integer id) {
        WeighingRecord record = weighingMapper.selectById(id);
        if (record == null) throw new BusinessException("称重记录不存在");
        if (record.getStatus() != 0) throw new BusinessException("只有待审核的记录可以删除");
        weighingMapper.deleteById(id);
    }

    private String generateWeighNo() {
        String dateStr = LocalDateTime.now().format(WEIGH_NO_FORMATTER);
        long count = weighingMapper.selectCount(
                new LambdaQueryWrapper<WeighingRecord>()
                        .likeLeft(WeighingRecord::getWeighNo, "WT" + dateStr));
        return String.format("WT%s%04d", dateStr, count + 1);
    }

    private WeighingRecordVO convertToVO(WeighingRecord record) {
        WeighingRecordVO vo = new WeighingRecordVO();
        vo.setId(record.getId());
        vo.setWeighNo(record.getWeighNo());
        vo.setReserveId(record.getReserveId());
        vo.setWeighbridgeId(record.getWeighbridgeId());
        vo.setGrossWeight(record.getGrossWeight());
        vo.setTareWeight(record.getTareWeight());
        vo.setNetWeight(record.getNetWeight());
        vo.setWeighTime(record.getWeighTime());
        vo.setDataSource(record.getDataSource());
        vo.setStatus(record.getStatus());
        vo.setRemark(record.getRemark());
        vo.setAuditUserId(record.getAuditUserId());
        vo.setAuditTime(record.getAuditTime());
        vo.setCreateUserId(record.getCreateUserId());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}