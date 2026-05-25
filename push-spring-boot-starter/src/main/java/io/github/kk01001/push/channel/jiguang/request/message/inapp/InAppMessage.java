package io.github.archer099.push.channel.jiguang.request.message.inapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InAppMessage {

    @JsonProperty("inapp_message")
    private Boolean inAppMessage;

}
