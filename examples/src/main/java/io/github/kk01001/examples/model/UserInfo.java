package io.github.kk01001.examples.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.kk01001.crypto.annotation.CryptoField;
import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import lombok.Data;

@Data
public class UserInfo {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Desensitize(type = DesensitizeType.NAME)
    private String name;

    @Desensitize(type = DesensitizeType.PHONE, startIndex = 2, endIndex = -2, maskChar = "#")
    private String phone;

    @CryptoField
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