package io.github.kk01001.nacos;

import com.alibaba.nacos.api.config.ConfigService;

/**
 * @author kk01001
 * date 2022/8/25 09:15
 */
public abstract class AbstractNacosConfig {

    /**
     * 配置文件变更时的回调函数
     *
     * @param content 配置内容
     */
    public abstract void onReceived(String content);

    /**
     * 配置文件标识
     *
     * @return 配置文件标识
     */
    public abstract String getDataId();

    /**
     * 配置文件所在组
     *
     * @return 配置文件所在组
     */
    public abstract String getGroup();

    public abstract ConfigService configService();

}
