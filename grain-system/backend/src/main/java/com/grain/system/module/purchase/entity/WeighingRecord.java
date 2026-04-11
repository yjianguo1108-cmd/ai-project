package com.grain.system.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_weighing_record")
public class WeighingRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String weighNo;

    private Integer reserveId;

    private Integer weighbridgeId;

    private BigDecimal grossWeight;

    private BigDecimal tareWeight;

    private BigDecimal netWeight;

    private LocalDateTime weighTime;

    private Integer dataSource;

    private Integer status;

    private Integer auditUserId;

    private LocalDateTime auditTime;

    private String remark;

    private Integer createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}