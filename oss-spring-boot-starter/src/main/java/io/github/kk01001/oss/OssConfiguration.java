package io.github.kk01001.oss;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.github.kk01001.oss.client.OssClient;
import io.github.kk01001.oss.client.S3OssClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author kk01001
 * date 2022-09-19 22:27:00
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "oss", name = "enable", havingValue = "true")
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean(S3OssClient.class)
    public OssClient ossClient(AmazonS3 amazonS3) {
        log.info("OssClient 初始化完成");
        return new S3OssClient(amazonS3);
    }

    /**
     * 参考文档
     * <a href="https://docs.aws.amazon.com/zh_cn/sdk-for-java/v1/developer-guide/credentials.html">...</a>
     * 区域选择这块
     * <a href="https://docs.aws.amazon.com/zh_cn/sdk-for-java/v1/developer-guide/java-dg-region-selection.html">...</a>
     *
     * @param ossProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AmazonS3 amazonS3(OssProperties ossProperties) {
        long nullSize = Stream.<String>builder()
                .add(ossProperties.getEndpoint())
                .add(ossProperties.getAccessSecret())
                .add(ossProperties.getAccessKey())
                .build()
                .filter(Objects::isNull)
                .count();
        if (nullSize > 0) {
            throw new RuntimeException("oss 配置错误,请检查");
        }
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxConnections(ossProperties.getMaxConnections());

        AWSCredentials awsCredentials = new BasicAWSCredentials(ossProperties.getAccessKey(),
                ossProperties.getAccessSecret());
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        AmazonS3 amazonS3 = AmazonS3Client.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ossProperties.getEndpoint(), ossProperties.getRegion()))
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .withClientConfiguration(clientConfiguration)
                .withPathStyleAccessEnabled(ossProperties.isPathStyleAccess())
                .build();
        log.info("amazonS3 初始化完成");
        return amazonS3;
    }
}
