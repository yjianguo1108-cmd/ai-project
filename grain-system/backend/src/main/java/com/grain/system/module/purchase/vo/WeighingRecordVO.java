package com.grain.system.module.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WeighingRecordVO {

    private Integer id;

    private String weighNo;

    private Integer reserveId;

    private String reserveNo;

    private Integer weighbridgeId;

    private String weighbridgeName;

    private BigDecimal grossWeight;

    private BigDecimal tareWeight;

    private BigDecimal netWeight;

    private LocalDateTime weighTime;

    private Integer dataSource;

    private String dataSourceName;

    private Integer status;

    private String statusName;

    private String remark;

    private Integer auditUserId;

    private String auditUserName;

    private LocalDateTime auditTime;

    private Integer createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}