package com.lamicore.coredemo.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDto {
    private String email;
    private String password;

    public LoginDto() {
    }

    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }


}
