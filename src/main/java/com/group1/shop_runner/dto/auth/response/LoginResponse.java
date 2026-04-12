    package com.group1.shop_runner.dto.auth.response;

    import lombok.Getter;

    @Getter
    public class LoginResponse {
        private Long id;
        private String token;
        private String username;
        private String role;
        private String status;

        public LoginResponse(Long id,String token, String username, String role, String status) {
            this.id = id;
            this.token = token;
            this.username = username;
            this.role = role;
            this.status = status;
        }
    }
