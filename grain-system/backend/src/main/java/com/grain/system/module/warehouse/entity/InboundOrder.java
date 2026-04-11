package com.grain.system.module.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_inbound_order")
public class InboundOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String inboundNo;

    private Integer purchaseOrderId;

    private Integer grainId;

    private Integer positionId;

    private BigDecimal netWeight;

    private Integer inboundType;

    private String inboundReason;

    private String attachmentUrl;

    private Integer status;

    private Integer confirmUserId;

    private LocalDateTime confirmTime;

    private Integer createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}