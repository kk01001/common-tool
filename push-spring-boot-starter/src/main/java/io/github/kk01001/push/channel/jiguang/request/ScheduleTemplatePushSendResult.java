package io.github.kk01001.push.channel.jiguang.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kk01001.push.channel.jiguang.request.other.TemplateResult;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleTemplatePushSendResult {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Data data;

    @lombok.Data
    public static class Data {

        @JsonProperty("schedule_list")
        private List<TemplateResult> results;

    }

}
