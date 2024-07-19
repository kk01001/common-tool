package io.github.kk01001.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kk01001
 * date 2022-09-19 22:30:00
 */
@ConfigurationProperties(prefix = "oss")
@Data
public class OssProperties {

    private boolean enable = true;

    private String accessKey;

    private String accessSecret;

    /**
     * endpoint 配置格式为
     * 通过外网访问OSS服务时，以URL的形式表示访问的OSS资源，详情请参见OSS访问域名使用规则。OSS的URL结构为[$Schema]://[$Bucket].[$Endpoint]/[$Object]
     * 。例如，您的Region为华东1（杭州），Bucket名称为examplebucket，Object访问路径为destfolder/example.txt，
     * 则外网访问地址为
     * <a href="https://examplebucket.oss-cn-hangzhou.aliyuncs.com/destfolder/example.txt">...</a>
     * <a href="https://help.aliyun.com/document_detail/375241.html">...</a>
     */
    private String endpoint;
    /**
     * refer com.amazonaws.regions.Regions;
     * 阿里云region 对应表
     * <a href="https://help.aliyun.com/document_detail/31837.htm?spm=a2c4g.11186623.0.0.695178eb0nD6jp">...</a>
     */
    private String region;

    /**
     * true path-style nginx 反向代理和S3默认支持 pathStyle模式 {http://endpoint/bucketname}
     * false supports virtual-hosted-style 阿里云等需要配置为 virtual-hosted-style 模式{http://bucketname.endpoint}
     * 只是url的显示不一样
     */
    private boolean pathStyleAccess = true;

    /**
     * 最大线程数，默认： 100
     */
    private Integer maxConnections = 100;

    /**
     * 存储空间名称
     */
    private String bucketName;
}
