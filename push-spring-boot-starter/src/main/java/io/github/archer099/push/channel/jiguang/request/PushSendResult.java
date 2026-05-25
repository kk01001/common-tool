package io.github.archer099.push.channel.jiguang.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PushSendResult {

    @JsonProperty("sendno")
    private String sendNo;

    @JsonProperty("msg_id")
    private String messageId;

}
