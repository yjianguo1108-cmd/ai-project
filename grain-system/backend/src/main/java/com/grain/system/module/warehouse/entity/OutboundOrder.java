package com.grain.system.module.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_outbound_order")
public class OutboundOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String outboundNo;

    private Integer salesOrderId;

    private Integer grainId;

    private Integer positionId;

    private BigDecimal planWeight;

    private BigDecimal actualWeight;

    private BigDecimal deviationRate;

    private Integer outboundType;

    private String outboundReason;

    private String attachmentUrl;

    private Integer batchNo;

    private Integer status;

    private Integer confirmUserId;

    private LocalDateTime confirmTime;

    private Integer createUserId;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String grainType;

    @TableField(exist = false)
    private String grainGrade;

    @TableField(exist = false)
    private String positionName;

    @TableField(exist = false)
    private String statusName;

    @TableField(exist = false)
    private String outboundTypeName;
}
