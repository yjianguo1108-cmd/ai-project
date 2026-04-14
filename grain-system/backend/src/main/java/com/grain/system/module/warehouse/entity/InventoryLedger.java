package com.grain.system.module.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_inventory_ledger")
public class InventoryLedger implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer inventoryId;

    private Integer ledgerType;

    private BigDecimal changeQty;

    private BigDecimal beforeQty;

    private BigDecimal afterQty;

    private Integer refBillType;

    private Integer refBillId;

    private Integer operatorId;

    private LocalDateTime operateTime;

    private String remark;
}
