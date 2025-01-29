package io.github.kk01001.push.channel.jiguang.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleTemplatePushSendParam extends TemplatePushSendParam {

    @JsonProperty("schedule_name")
    private String scheduleName;

    @JsonProperty("trigger")
    private SchedulePushSendParam.Trigger trigger;

}
