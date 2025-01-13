package com.example.hotel.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;
    
    private String password;  // 创建时必填,更新时选填
    
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @NotBlank(message = "角色不能为空")
    private String role;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private String avatar;
    
    private boolean enabled = true;
} 