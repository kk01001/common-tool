package io.github.kk01001.push.channel.jiguang.request.callback;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback {

    @JsonProperty("url")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String url;

    /**
     * 取值参考：{@link CallbackType}
     */
    @JsonProperty("type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer type;

    @JsonProperty("params")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> params;

}
