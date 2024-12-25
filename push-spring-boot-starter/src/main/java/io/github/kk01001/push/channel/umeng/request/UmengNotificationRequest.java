package io.github.kk01001.push.channel.umeng.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:51:00
 * @description
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UmengNotificationRequest {

    /**
     * 必填，应用唯一标识
     */
    @JsonProperty("appkey")
    private String appKey;

    /**
     * 必填，时间戳
     * 10位或者13位均可
     * 时间戳有效期为10分钟
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 必填，消息发送类型
     * 其值可以为:
     * - unicast: 单播
     * - listcast: 列播，要求不超过500个device_token
     * - filecast: 文件播，多个device_token可通过文件形式批量发送
     * - broadcast: 广播
     * - groupcast: 组播，按照filter筛选用户群
     * - customizedcast: 通过alias进行推送
     */
    @JsonProperty("type")
    private String type;

    /**
     * 当type=unicast时,必填,表示指定的单个设备
     * 当type=listcast时,必填,要求不超过500个,以英文逗号分隔
     */
    @JsonProperty("device_tokens")
    private String deviceTokens;

    /**
     * 当type=customizedcast时,必填
     * alias的类型, alias_type可由开发者自定义
     * 开发者在SDK中调用setAlias(alias, alias_type)时所设置的alias_type
     */
    @JsonProperty("alias_type")
    private String aliasType;

    /**
     * 当type=customizedcast时,选填(此参数和file_id二选一)
     * 开发者填写自己的alias,要求不超过500个alias
     * 多个alias以英文逗号间隔
     * 在SDK中调用setAlias(alias, alias_type)时所设置的alias
     */
    @JsonProperty("alias")
    private String alias;

    /**
     * 当type=filecast时，必填，file内容为多条device_token，以回车符分割
     * 当type=customizedcast时，选填(此参数和alias二选一)
     * file内容为多条alias，以回车符分隔
     * 注意同一个文件内的alias所对应的alias_type必须和接口参数alias_type一致
     * 使用文件播需要先调用文件上传接口获取file_id
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * 当type=groupcast时，filter或group_id至少填一个
     * filter代表用户筛选条件，如用户标签、渠道等
     * filter的内容长度最大为3000B
     */
    @JsonProperty("filter")
    private Map<String, Object> filter;

    /**
     * 当type=groupcast时，filter或group_id至少填一个
     * group_id代表用户分群ID
     * 当filter和group_id同时存在时，只取group_id
     */
    @JsonProperty("group_id")
    private String groupId;

    /**
     * 必填，JSON格式，具体消息内容
     * Android最大为2048B
     */
    @JsonProperty("payload")
    private Payload payload;

    /**
     * 可选，发送策略
     */
    @JsonProperty("policy")
    private Policy policy;

    /**
     * 可选，true正式模式，false测试模式
     * 默认为true
     * 广播、组播下的测试模式只会将消息发给测试设备
     * 单播、文件播不受测试设备限制
     */
    @JsonProperty("production_mode")
    private String productionMode;

    /**
     * 可选，发送消息描述
     * 建议填写
     */
    @JsonProperty("description")
    private String description;

    /**
     * 可选，友盟消息自分类
     * 0：资讯营销类消息
     * 1：服务与通讯类消息
     */
    @JsonProperty("category")
    private Integer category;

    /**
     * 自定义回执参数
     */
    @JsonProperty("callback_params")
    private Map<String, Object> callbackParams;

    /**
     * 可选，厂商通道相关的特殊配置
     */
    @JsonProperty("channel_properties")
    private ChannelProperties channelProperties;

    /**
     * 可选，本地通知相关的特殊配置
     */
    @JsonProperty("local_properties")
    private LocalProperties localProperties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Payload {

        /**
         * 必填，消息类型
         * - notification: 通知
         * - message: 消息
         */
        @JsonProperty("display_type")
        private String displayType;

        /**
         * 必填，消息体
         * 当display_type=message时，body的内容只需填写custom字段
         * 当display_type=notification时，body包含如下参数:
         */
        @JsonProperty("body")
        private Body body;

        /**
         * 可选，用户自定义key-value
         * 可以配合消息到达后，打开App/URL/Activity使用
         */
        @JsonProperty("extra")
        private Map<String, Object> extra;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Body {

        /**
         * 必填，通知标题
         */
        @JsonProperty("title")
        private String title;

        /**
         * 必填，通知文字描述
         */
        @JsonProperty("text")
        private String text;

        /**
         * 可选, 通知栏提示文字
         */
        @JsonProperty("ticker")
        private String ticker;

        /**
         * 可选, 最多120个字符大文本
         */
        @JsonProperty("big_body")
        private String bigBody;

        /**
         * 可选，0：不重弹；1：重弹
         * 默认值是0
         */
        @JsonProperty("re_pop")
        private Integer rePop;

        /**
         * 可选，状态栏图标ID
         * R.drawable.[smallIcon]
         * 如果没有，默认使用应用图标
         */
        @JsonProperty("icon")
        private String icon;

        /**
         * 可选，通知栏大图标的URL链接
         * 该字段要求以http或者https开头
         * 图片建议不大于100KB
         */
        @JsonProperty("img")
        private String img;

        /**
         * 可选，消息下方展示大图
         * 支持自有通道消息展示
         * 厂商通道展示大图目前仅支持小米
         */
        @JsonProperty("expand_image")
        private String expandImage;

        /**
         * 可选，通知声音
         * R.raw.[sound]
         * 如果该字段为空，采用SDK默认的声音
         */
        @JsonProperty("sound")
        private String sound;

        /**
         * 可选，用于标识该通知采用的样式
         * 默认为0
         */
        @JsonProperty("builder_id")
        private Integer builderId;

        /**
         * 可选，角标设置数字
         * 范围为1~99
         */
        @JsonProperty("set_badge")
        private Integer setBadge;

        /**
         * 可选，角标设置数字
         * 范围为1~99
         */
        @JsonProperty("add_badge")
        private Integer addBadge;

        /**
         * 可选，收到通知是否震动
         * 默认为"true"
         */
        @JsonProperty("play_vibrate")
        private String playVibrate;

        /**
         * 可选，收到通知是否闪灯
         * 默认为"true"
         */
        @JsonProperty("play_lights")
        private String playLights;

        /**
         * 可选，收到通知是否发出声音
         * 默认为"true"
         */
        @JsonProperty("play_sound")
        private String playSound;

        /**
         * 可选，点击"通知"的后续行为
         * 默认为"go_app"
         * 值可以为:
         * - "go_app": 打开应用
         * - "go_url": 跳转到URL
         * - "go_activity": 打开特定的activity
         * - "go_custom": 用户自定义内容
         */
        @JsonProperty("after_open")
        private String afterOpen;

        /**
         * 当after_open=go_url时，必填
         * 通知栏点击后跳转的URL
         * 要求以http或者https开头
         */
        @JsonProperty("url")
        private String url;

        /**
         * 当after_open=go_activity时，必填
         * 通知栏点击后打开的Activity
         */
        @JsonProperty("activity")
        private String activity;

        /**
         * 当display_type=message时,必填
         * 当display_type=notification且after_open=go_custom时，必填
         * 用户自定义内容，可以为字符串或者JSON格式
         */
        @JsonProperty("custom")
        private Object custom;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Policy {

        /**
         * 可选，定时发送时，若不填写表示立即发送
         * 定时发送时间不能小于当前时间
         * 格式:"yyyy-MM-dd HH:mm:ss"
         * 注意，start_time只对任务类消息生效
         */
        @JsonProperty("start_time")
        private String startTime;

        /**
         * 可选，消息过期时间
         * 其值不可小于发送时间或者start_time(如果填写了的话)
         * 如果不填写此参数，默认为3天后过期
         * 格式同start_time
         */
        @JsonProperty("expire_time")
        private String expireTime;

        /**
         * 可选，发送限速
         * 每秒发送的最大条数
         * 最小值1000
         */
        @JsonProperty("max_send_num")
        private Integer maxSendNum;

        /**
         * 可选，消息发送接口对任务类消息的幂等性保证
         * 强烈建议开发者在发送任务类消息时填写这个字段
         * 友盟服务端会根据这个字段对消息做去重避免重复发送
         */
        @JsonProperty("out_biz_no")
        private String outBizNo;

        /**
         * 可选，只对display_type=notification的消息生效
         * 设置为true会过滤关闭通知栏消息的设备
         * 以免占用厂商额度
         */
        @JsonProperty("notification_closed_filter")
        private Boolean notificationClosedFilter;

        /**
         * 可选，如果配置为true,在通知栏关闭时会通过应用内弹窗展示
         * 该功能为推送专业版（Pro）高级能力
         * SDK v6.6.3及以上版本支持
         */
        @JsonProperty("in_app")
        private InApp inApp;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class InApp {

        /**
         * 可选，如果配置为true,在通知栏关闭时会通过应用内弹窗展示
         */
        @JsonProperty("in_app")
        private Boolean inApp;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class ChannelProperties {

        /**
         * 系统弹窗，走厂商通道时必填
         * 只有display_type=notification时有效
         * 表示华为、小米、oppo、vivo、魅族的设备离线时走系统通道下发时打开指定页面acitivity的完整包路径
         */
        @JsonProperty("channel_activity")
        private String channelActivity;

        /**
         * 小米channel_id
         * 具体使用及限制请参考小米推送文档
         */
        @JsonProperty("xiaomi_channel_id")
        private String xiaomiChannelId;

        /**
         * vivo消息二级分类参数
         * 友盟侧只进行参数透传，不做合法性校验
         */
        @JsonProperty("vivo_category")
        private String vivoCategory;

        /**
         * vivo角标功能
         * 需要客户端先完成系统API接入
         * 否则发消息时会报错，错误码为10089
         */
        @JsonProperty("vivo_addbadge")
        private String vivoAddbadge;

        /**
         * 可选，android8以上推送消息需要新建通道
         * 否则消息无法触达用户
         * push sdk 6.0.5及以上创建了默认的通道:upush_default
         */
        @JsonProperty("oppo_channel_id")
        private String oppoChannelId;

        /**
         * 可选，OPPO消息类别
         * 参考OPPO官方文档
         */
        @JsonProperty("oppo_category")
        private String oppoCategory;

        /**
         * 可选，OPPO通知栏消息提醒等级
         * 1-通知栏，2-通知栏+锁屏
         * 16-通知栏+锁屏+横幅+震动+铃声
         * 使用该参数时，oppo_category必传
         */
        @JsonProperty("oppo_notify_level")
        private String oppoNotifyLevel;

        /**
         * 可选，应用入口Activity类全路径
         * 主要用于华为通道角标展示
         */
        @JsonProperty("main_activity")
        private String mainActivity;

        /**
         * 可选，华为&荣耀消息分类
         * LOW：资讯营销类消息
         * NORMAL：服务与通讯类消息
         */
        @JsonProperty("huawei_channel_importance")
        private String huaweiChannelImportance;

        /**
         * 可选，华为自分类消息类型
         * 参考华为消息分类文档
         */
        @JsonProperty("huawei_channel_category")
        private String huaweiChannelCategory;

        /**
         * 可选，fcm通道开关
         * 0不使用，1使用
         */
        @JsonProperty("channel_fcm")
        private String channelFcm;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class LocalProperties {

        /**
         * 可选，华为本地通知category
         * 请严格按照华为官方文档设置
         */
        @JsonProperty("category")
        private String category;

        /**
         * 可选，华为本地通知importance
         * 请严格按照华为官方文档设置
         */
        @JsonProperty("importance")
        private String importance;
    }
}

