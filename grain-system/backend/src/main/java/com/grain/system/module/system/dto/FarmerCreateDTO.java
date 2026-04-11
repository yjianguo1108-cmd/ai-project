package com.grain.system.module.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FarmerCreateDTO {

    @NotNull(message = "关联用户ID不能为空")
    private Integer userId;

    /** 身份证号明文，后端加密存储 */
    private String idCard;

    /** 银行卡号明文，后端加密存储 */
    private String bankCard;

    private String bankName;

    /** 常售粮食种类 JSON数组，如 [1,3] */
    private String preferredGrainTypes;

    private String remark;
}
