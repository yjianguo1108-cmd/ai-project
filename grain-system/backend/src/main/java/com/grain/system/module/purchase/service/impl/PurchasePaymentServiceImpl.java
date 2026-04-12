package com.grain.system.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.constant.RedisKey;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.dto.PurchasePaymentCreateDTO;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.entity.PurchasePayment;
import com.grain.system.module.purchase.mapper.PurchaseOrderMapper;
import com.grain.system.module.purchase.mapper.PurchasePaymentMapper;
import com.grain.system.module.purchase.service.PurchasePaymentService;
import com.grain.system.module.purchase.vo.PurchasePaymentVO;
import com.grain.system.module.system.entity.Farmer;
import com.grain.system.module.system.entity.User;
import com.grain.system.module.system.mapper.FarmerMapper;
import com.grain.system.module.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchasePaymentServiceImpl implements PurchasePaymentService {

    private final PurchasePaymentMapper paymentMapper;
    private final PurchaseOrderMapper orderMapper;
    private final FarmerMapper farmerMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter PAYMENT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<PurchasePaymentVO> getPaymentPage(int page, int size, String keyword, Integer orderId) {
        Page<PurchasePaymentVO> pageParam = new Page<>(page, size);
        return paymentMapper.selectPaymentPage(pageParam, keyword, orderId);
    }

    @Override
    public PurchasePaymentVO getPaymentById(Integer id) {
        PurchasePayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException("付款记录不存在");
        return convertToVO(payment);
    }

    @Override
    @Transactional
    public void createPayment(PurchasePaymentCreateDTO dto, Integer operatorId) {
        PurchaseOrder order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("收购单据不存在");

        BigDecimal unpaidAmount = order.getTotalAmount().subtract(order.getPaidAmount());
        if (dto.getPayAmount().compareTo(unpaidAmount) > 0) {
            throw new BusinessException("付款金额不能超过未付金额：" + unpaidAmount);
        }

        PurchasePayment payment = new PurchasePayment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(dto.getOrderId());
        payment.setFarmerId(order.getFarmerId());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayMethod(dto.getPayMethod());
        payment.setPayTime(dto.getPayTime() != null ? dto.getPayTime() : LocalDateTime.now());
        payment.setPaymentAccount(dto.getPaymentAccount());
        payment.setRecipientName(dto.getRecipientName());
        payment.setRemark(dto.getRemark());
        payment.setOperatorId(operatorId);
        paymentMapper.insert(payment);

        BigDecimal newPaidAmount = order.getPaidAmount().add(dto.getPayAmount());
        order.setPaidAmount(newPaidAmount);
        if (newPaidAmount.compareTo(order.getTotalAmount()) >= 0) {
            order.setPaymentStatus(2);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus(1);
        }
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void deletePayment(Integer id) {
        PurchasePayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException("付款记录不存在");

        PurchaseOrder order = orderMapper.selectById(payment.getOrderId());
        if (order != null) {
            BigDecimal newPaidAmount = order.getPaidAmount().subtract(payment.getPayAmount());
            if (newPaidAmount.compareTo(BigDecimal.ZERO) < 0) newPaidAmount = BigDecimal.ZERO;
            order.setPaidAmount(newPaidAmount);
            if (newPaidAmount.compareTo(order.getTotalAmount()) >= 0) {
                order.setPaymentStatus(2);
            } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                order.setPaymentStatus(1);
            } else {
                order.setPaymentStatus(0);
            }
            orderMapper.updateById(order);
        }
        paymentMapper.deleteById(id);
    }

    @Override
    public List<PurchaseOrder> getAvailableOrdersForPayment() {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 2)
               .in(PurchaseOrder::getPaymentStatus, 0, 1)
               .orderByDesc(PurchaseOrder::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    private String generatePaymentNo() {
        String dateStr = LocalDateTime.now().format(PAYMENT_NO_FORMATTER);
        String redisKey = RedisKey.ORDER_PO_SEQUENCE + ":payment:" + dateStr;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) {
            sequence = 1L;
        }
        return String.format("PP%s%04d", dateStr, sequence);
    }

    private PurchasePaymentVO convertToVO(PurchasePayment payment) {
        PurchasePaymentVO vo = new PurchasePaymentVO();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderId(payment.getOrderId());
        vo.setFarmerId(payment.getFarmerId());
        vo.setPayAmount(payment.getPayAmount());
        vo.setPayMethod(payment.getPayMethod());
        vo.setPayTime(payment.getPayTime());
        vo.setPaymentAccount(payment.getPaymentAccount());
        vo.setRecipientName(payment.getRecipientName());
        vo.setFlowNo(payment.getFlowNo());
        vo.setRemark(payment.getRemark());
        vo.setOperatorId(payment.getOperatorId());
        vo.setCreateTime(payment.getCreateTime());

        vo.setPayMethodName(switch (payment.getPayMethod()) { case 1 -> "现金"; case 2 -> "银行转账"; case 3 -> "微信支付"; case 4 -> "支付宝"; default -> "未知"; });

        if (payment.getOrderId() != null) {
            PurchaseOrder order = orderMapper.selectById(payment.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setOrderTotalAmount(order.getTotalAmount());
                vo.setOrderPaidAmount(order.getPaidAmount());
                vo.setOrderUnpaidAmount(order.getTotalAmount().subtract(order.getPaidAmount()));
            }
        }

        if (payment.getFarmerId() != null) {
            Farmer farmer = farmerMapper.selectById(payment.getFarmerId());
            if (farmer != null && farmer.getUserId() != null) {
                User user = userMapper.selectById(farmer.getUserId());
                if (user != null) {
                    vo.setFarmerName(user.getRealName());
                }
            }
        }

        if (payment.getOperatorId() != null) {
            User operator = userMapper.selectById(payment.getOperatorId());
            if (operator != null) {
                vo.setOperatorName(operator.getRealName());
            }
        }

        return vo;
    }
}