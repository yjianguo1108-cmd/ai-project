package com.grain.system.module.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sales_collection_record")
public class SalesCollectionRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer salesOrderId;

    private Integer orgId;

    private String remindContent;

    private LocalDateTime remindTime;

    private Integer operatorId;

    private LocalDateTime createTime;
}
