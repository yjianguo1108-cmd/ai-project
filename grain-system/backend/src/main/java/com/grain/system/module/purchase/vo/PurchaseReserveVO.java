package com.grain.system.module.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseReserveVO {

    private Integer id;

    private String reserveNo;

    private Integer farmerId;

    private String farmerName;

    private String farmerPhone;

    private Integer grainId;

    private String grainType;

    private String grainGrade;

    private BigDecimal estimatedWeight;

    private LocalDateTime reserveTime;

    private Integer status;

    private String statusName;

    private String rejectReason;

    private Integer auditUserId;

    private String auditUserName;

    private LocalDateTime auditTime;

    private Integer createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}