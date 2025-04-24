// dto/TwoFactorCodeDTO.java
package com.Iviinvest.dto;

public class TwoFactorCodeDTO {
    private String email;

    // construtor, getters, setters


    public TwoFactorCodeDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
