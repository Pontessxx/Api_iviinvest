// dto/TwoFactorRequestDTO.java
package com.Iviinvest.dto;

public class TwoFactorRequestDTO {
    private String email;
    private String token;

    // getters e setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
