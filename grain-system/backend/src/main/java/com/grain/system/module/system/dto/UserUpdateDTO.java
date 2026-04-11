package com.grain.system.module.system.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {
    private String realName;

    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    private String email;

    private List<Integer> roleIds;
}
