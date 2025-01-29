package io.github.kk01001.push.channel.fcm.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2024-10-12 16:25:00
 * @description
 */
@Data
public class GoogleAuthTokenDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;
}
