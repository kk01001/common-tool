package io.github.kk01001.push.channel.jiguang.request.other;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kk01001.push.channel.jiguang.request.audience.Audience;
import lombok.Data;

import java.util.Map;

@Data
public class TemplateParam {

    /**
     * 两种格式
     * 字符串："all"
     * {@link Audience}对象: {"tag":[],"tag_and":[],"tag_not":[],"alias":[],"registration_id":[],"segment":[],"abtest":[],"live_activity_id":"","file":{"file_id":""}}
     */
    @JsonProperty("audience")
    private Object audience;

    @JsonProperty("keys")
    private Map<String, String> keys;

    @JsonProperty("trace_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String traceId;

}
