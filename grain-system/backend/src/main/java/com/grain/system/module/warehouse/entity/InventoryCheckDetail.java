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
@TableName("t_inventory_check_detail")
public class InventoryCheckDetail implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer checkId;

    private Integer inventoryId;

    private Integer grainId;

    private Integer positionId;

    @TableField(exist = false)
    private String grainType;

    @TableField(exist = false)
    private String grainGrade;

    @TableField(exist = false)
    private String positionName;

    private BigDecimal bookQty;

    private BigDecimal actualQty;

    private BigDecimal diffQty;

    private BigDecimal diffRate;

    private Integer isOverThreshold;

    private String photoUrls;

    private Integer handleType;

    private String handleReason;

    private Integer handleUserId;

    private LocalDateTime handleTime;

    private Integer operatorId;

    private LocalDateTime checkTime;
}
