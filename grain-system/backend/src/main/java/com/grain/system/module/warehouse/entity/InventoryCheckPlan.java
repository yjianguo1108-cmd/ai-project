package com.grain.system.module.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_inventory_check")
public class InventoryCheckPlan implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String checkNo;

    private Integer checkType;

    private String scopeGrainIds;

    private String scopePositionIds;

    private String operatorIds;

    private LocalDateTime deadline;

    private Integer status;

    private Integer totalItems;

    private Integer checkedItems;

    private java.math.BigDecimal matchRate;

    private String reportUrl;

    private Integer createUserId;

    private LocalDateTime createTime;

    private LocalDateTime completeTime;
}
