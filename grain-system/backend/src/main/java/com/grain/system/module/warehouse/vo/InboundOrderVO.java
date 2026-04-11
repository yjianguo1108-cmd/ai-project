package com.grain.system.module.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InboundOrderVO {

    private Integer id;

    private String inboundNo;

    private Integer purchaseOrderId;

    private String purchaseOrderNo;

    private Integer grainId;

    private String grainType;

    private String grainGrade;

    private Integer positionId;

    private String positionName;

    private BigDecimal netWeight;

    private Integer inboundType;

    private String inboundReason;

    private String attachmentUrl;

    private Integer status;

    private String statusName;

    private Integer confirmUserId;

    private String confirmUserName;

    private LocalDateTime confirmTime;

    private Integer createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}