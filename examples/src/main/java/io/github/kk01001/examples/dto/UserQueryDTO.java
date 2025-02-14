package io.github.kk01001.examples.dto;

import io.github.kk01001.crypto.annotation.CryptoField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2025-02-14 09:49:00
 * @description
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryDTO {

    @CryptoField
    private String username;

}
