package com.grain.system.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.purchase.dto.PurchasePaymentCreateDTO;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.entity.PurchasePayment;
import com.grain.system.module.purchase.mapper.PurchaseOrderMapper;
import com.grain.system.module.purchase.mapper.PurchasePaymentMapper;
import com.grain.system.module.purchase.service.PurchasePaymentService;
import com.grain.system.module.purchase.vo.PurchasePaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PurchasePaymentServiceImpl implements PurchasePaymentService {

    private final PurchasePaymentMapper paymentMapper;
    private final PurchaseOrderMapper orderMapper;

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
        payment.setFarmerId(dto.getFarmerId());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayMethod(dto.getPayMethod());
        payment.setPayTime(dto.getPayTime() != null ? dto.getPayTime() : LocalDateTime.now());
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

    private String generatePaymentNo() {
        String dateStr = LocalDateTime.now().format(PAYMENT_NO_FORMATTER);
        long count = paymentMapper.selectCount(
                new LambdaQueryWrapper<PurchasePayment>()
                        .likeLeft(PurchasePayment::getPaymentNo, "PP" + dateStr));
        return String.format("PP%s%04d", dateStr, count + 1);
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
        vo.setFlowNo(payment.getFlowNo());
        vo.setRemark(payment.getRemark());
        vo.setOperatorId(payment.getOperatorId());
        vo.setCreateTime(payment.getCreateTime());
        return vo;
    }
}