package com.grain.system.module.warehouse.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CheckPlanCreateDTO {

    @NotNull(message = "盘点类型不能为空")
    private Integer checkType;

    @NotEmpty(message = "请选择粮食品种")
    private List<Integer> grainIds;

    @NotEmpty(message = "请选择储位")
    private List<Integer> positionIds;

    @NotNull(message = "截止日期不能为空")
    private LocalDateTime deadline;

    @NotEmpty(message = "请选择操作员")
    private List<Integer> operatorIds;
}
