package com.group1.shop_runner.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Please enter email...")
    private String email;
    @NotBlank(message = "Please enter password...")
    private String password;
}
