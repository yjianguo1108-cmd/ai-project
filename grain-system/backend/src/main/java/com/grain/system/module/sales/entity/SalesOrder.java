package com.grain.system.module.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sales_order")
public class SalesOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;

    private Integer orgId;

    private Integer grainId;

    private Integer positionId;

    private BigDecimal planWeight;

    private BigDecimal actualOutWeight;

    private BigDecimal refPrice;

    private BigDecimal actualPrice;

    private BigDecimal totalAmount;

    private BigDecimal actualAmount;

    private BigDecimal receivedAmount;

    private BigDecimal reservedStock;

    private Integer status;

    private Integer outStatus;

    private Integer paymentStatus;

    private String voidReason;

    private String attachmentUrl;

    private Integer auditUserId;

    private LocalDateTime auditTime;

    private Integer createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
