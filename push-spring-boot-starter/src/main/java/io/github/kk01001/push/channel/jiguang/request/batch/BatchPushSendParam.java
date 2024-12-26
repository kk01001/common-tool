package io.github.kk01001.push.channel.jiguang.request.batch;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class BatchPushSendParam {

    @JsonProperty("pushlist")
    private Map<String, BatchPushParam> sendParam;

}
