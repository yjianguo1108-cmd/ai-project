package com.grain.system.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateDTO {

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 32, message = "账号长度3-32位")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    private String email;

    @NotEmpty(message = "至少分配一个角色")
    private List<Integer> roleIds;
}
