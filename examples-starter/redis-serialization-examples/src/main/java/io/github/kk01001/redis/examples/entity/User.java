package io.github.kk01001.redis.examples.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User entity for serialization testing
 * 
 * @author kk01001
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    
    private String id;
    private String name;
    private String email;
    private Integer age;
    private String address;
    private String phone;
    private String company;
    private String department;
    private String position;
    private String city;
    private String province;
    private String country;
    private String zipCode;
    private String idCard;
    private String bankCard;
    private String emergencyContact;
    private String emergencyPhone;
    private String hobby;
    private String education;
    private String graduateSchool;
}
