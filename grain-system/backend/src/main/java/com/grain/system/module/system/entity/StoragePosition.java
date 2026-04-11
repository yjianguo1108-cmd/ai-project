package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_storage_position")
public class StoragePosition {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String positionCode;
    private String positionName;
    private BigDecimal capacity;
    private BigDecimal currentStock;

    /** 状态 0不可用/1可用/2维修中 */
    private Integer status;
    private Integer sensorDeviceId;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
