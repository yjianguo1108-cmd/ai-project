package com.grain.system.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.constant.RedisKey;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.dto.PurchaseOrderCreateDTO;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.entity.PurchaseReserve;
import com.grain.system.module.purchase.entity.WeighingRecord;
import com.grain.system.module.purchase.mapper.PurchaseOrderMapper;
import com.grain.system.module.purchase.mapper.PurchaseReserveMapper;
import com.grain.system.module.purchase.mapper.WeighingRecordMapper;
import com.grain.system.module.purchase.service.PurchaseOrderService;
import com.grain.system.module.purchase.vo.PurchaseOrderVO;
import com.grain.system.module.system.entity.Farmer;
import com.grain.system.module.system.entity.Grain;
import com.grain.system.module.system.entity.User;
import com.grain.system.module.system.mapper.FarmerMapper;
import com.grain.system.module.system.mapper.GrainMapper;
import com.grain.system.module.system.mapper.UserMapper;
import com.grain.system.module.warehouse.service.InboundOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderMapper orderMapper;
    private final PurchaseReserveMapper reserveMapper;
    private final WeighingRecordMapper weighingMapper;
    private final FarmerMapper farmerMapper;
    private final GrainMapper grainMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final InboundOrderService inboundOrderService;

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<PurchaseOrderVO> getOrderPage(int page, int size, String keyword, Integer status, Integer farmerId, Integer paymentStatus) {
        Page<PurchaseOrderVO> pageParam = new Page<>(page, size);
        return orderMapper.selectOrderPage(pageParam, keyword, status, farmerId, paymentStatus);
    }

    @Override
    public PurchaseOrderVO getOrderById(Integer id) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("收购单据不存在");
        return convertToVO(order);
    }

    @Override
    @Transactional
    public void createOrder(PurchaseOrderCreateDTO dto, Integer operatorId) {
        WeighingRecord weighing = weighingMapper.selectById(dto.getWeighRecordId());
        if (weighing == null) throw new BusinessException("称重记录不存在");
        if (weighing.getStatus() != 1) throw new BusinessException("称重记录未审核确认，无法创建收购单");

        LambdaQueryWrapper<PurchaseOrder> orderCheck = new LambdaQueryWrapper<>();
        orderCheck.eq(PurchaseOrder::getWeighRecordId, dto.getWeighRecordId());
        if (orderMapper.selectCount(orderCheck) > 0) {
            throw new BusinessException("该称重记录已创建收购单据，不能重复创建");
        }

        Grain grain = grainMapper.selectById(dto.getGrainId());
        if (grain == null) throw new BusinessException("粮食品种不存在");

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setFarmerId(dto.getFarmerId());
        order.setGrainId(dto.getGrainId());
        order.setWeighRecordId(dto.getWeighRecordId());
        order.setActualWeight(dto.getActualWeight());
        order.setRefPrice(grain.getRefPurchasePrice());
        order.setActualPrice(dto.getActualPrice());
        order.setTotalAmount(dto.getActualWeight().multiply(dto.getActualPrice()));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentStatus(0);
        order.setStatus(0);
        order.setCreateUserId(operatorId);
        orderMapper.insert(order);
    }

    @Override
    @Transactional
    public void updateOrder(Integer id, PurchaseOrderCreateDTO dto, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("收购单据不存在");
        if (order.getStatus() != 0) throw new BusinessException("当前状态不允许修改");

        Grain grain = grainMapper.selectById(dto.getGrainId());
        if (grain == null) throw new BusinessException("粮食品种不存在");

        order.setGrainId(dto.getGrainId());
        order.setActualWeight(dto.getActualWeight());
        order.setActualPrice(dto.getActualPrice());
        order.setTotalAmount(dto.getActualWeight().multiply(dto.getActualPrice()));
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void auditOrder(Integer id, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("收购单据不存在");
        if (order.getStatus() != 0) throw new BusinessException("当前状态不允许审核");

        order.setStatus(2);
        order.setAuditUserId(operatorId);
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Integer id, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("收购单据不存在");
        if (order.getStatus() != 2) throw new BusinessException("只有审核通过的单据可以确认完成");

        order.setStatus(3);
        orderMapper.updateById(order);

        inboundOrderService.createInboundForPurchaseOrder(order.getId(), null, operatorId);
    }

    @Override
    @Transactional
    public void voidOrder(Integer id, String reason, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("收购单据不存在");
        if (order.getStatus() == 3 || order.getStatus() == 4) throw new BusinessException("当前状态不允许作废");

        order.setStatus(4);
        order.setVoidReason(reason);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("收购单据不存在");
        if (order.getStatus() != 0) throw new BusinessException("只有草稿状态可以删除");
        orderMapper.deleteById(id);
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(ORDER_NO_FORMATTER);
        String redisKey = RedisKey.ORDER_PO_SEQUENCE + ":" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) {
            sequence = 1L;
        }
        return String.format("PO%s%04d", dateStr, sequence);
    }

    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setFarmerId(order.getFarmerId());
        vo.setGrainId(order.getGrainId());
        vo.setWeighRecordId(order.getWeighRecordId());
        vo.setActualWeight(order.getActualWeight());
        vo.setRefPrice(order.getRefPrice());
        vo.setActualPrice(order.getActualPrice());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPaidAmount(order.getPaidAmount());
        vo.setUnpaidAmount(order.getTotalAmount().subtract(order.getPaidAmount()));
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setStatus(order.getStatus());
        vo.setVoidReason(order.getVoidReason());
        vo.setAttachmentUrl(order.getAttachmentUrl());
        vo.setCreateUserId(order.getCreateUserId());
        vo.setCreateTime(order.getCreateTime());
        vo.setAuditUserId(order.getAuditUserId());
        vo.setAuditTime(order.getAuditTime());
        vo.setUpdateTime(order.getUpdateTime());

        vo.setPaymentStatusName(switch (order.getPaymentStatus()) { case 0 -> "未付款"; case 1 -> "部分付款"; case 2 -> "已付清"; default -> "未知"; });
        vo.setStatusName(switch (order.getStatus()) { case 0 -> "草稿"; case 1 -> "待审核"; case 2 -> "审核通过"; case 3 -> "已完成"; case 4 -> "已作废"; default -> "未知"; });

        if (order.getFarmerId() != null) {
            Farmer farmer = farmerMapper.selectById(order.getFarmerId());
            if (farmer != null && farmer.getUserId() != null) {
                User user = userMapper.selectById(farmer.getUserId());
                if (user != null) {
                    vo.setFarmerName(user.getRealName());
                    vo.setFarmerPhone(user.getPhone());
                }
            }
        }

        if (order.getGrainId() != null) {
            Grain grain = grainMapper.selectById(order.getGrainId());
            if (grain != null) {
                vo.setGrainType(grain.getGrainType());
                vo.setGrainGrade(grain.getGrainGrade());
            }
        }

        if (order.getWeighRecordId() != null) {
            WeighingRecord weighing = weighingMapper.selectById(order.getWeighRecordId());
            if (weighing != null) {
                vo.setWeighNo(weighing.getWeighNo());
            }
        }

        if (order.getCreateUserId() != null) {
            User createUser = userMapper.selectById(order.getCreateUserId());
            if (createUser != null) {
                vo.setCreateUserName(createUser.getRealName());
            }
        }

        if (order.getAuditUserId() != null) {
            User auditUser = userMapper.selectById(order.getAuditUserId());
            if (auditUser != null) {
                vo.setAuditUserName(auditUser.getRealName());
            }
        }

        return vo;
    }
}