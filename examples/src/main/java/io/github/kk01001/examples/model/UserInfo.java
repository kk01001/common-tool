package io.github.kk01001.examples.model;

import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import lombok.Data;

@Data
public class UserInfo {
    
    @Desensitize(type = DesensitizeType.NAME)
    private String name;

    @Desensitize(type = DesensitizeType.PHONE, startIndex = 2, endIndex = -2, maskChar = "#")
    private String phone;
    
    @Desensitize(type = DesensitizeType.EMAIL)
    private String email;
    
    @Desensitize(type = DesensitizeType.ID_CARD)
    private String idCard;
    
    @Desensitize(type = DesensitizeType.BANK_CARD)
    private String bankCard;
    
    @Desensitize(type = DesensitizeType.ADDRESS)
    private String address;
    
    @Desensitize(type = DesensitizeType.PASSWORD)
    private String password;
} 