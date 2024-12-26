package io.github.kk01001.push.channel.jiguang.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kk01001.push.channel.jiguang.request.other.TemplateParam;
import lombok.Data;

import java.util.List;

@Data
public class TemplatePushSendParam {

    @JsonProperty("id")
    private String id;

    @JsonProperty("params")
    private List<TemplateParam> params;

}
