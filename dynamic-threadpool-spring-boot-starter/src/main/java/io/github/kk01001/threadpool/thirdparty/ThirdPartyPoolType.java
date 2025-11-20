package io.github.kk01001.threadpool.thirdparty;

/**
 * 第三方线程池类型枚举
 * 
 * @author kk01001
 */
public enum ThirdPartyPoolType {
    
    /**
     * Tomcat Web 服务器线程池
     */
    TOMCAT("tomcat", "Tomcat Web Server"),
    
    /**
     * Undertow Web 服务器线程池
     */
    UNDERTOW("undertow", "Undertow Web Server"),
    
    /**
     * Jetty Web 服务器线程池
     */
    JETTY("jetty", "Jetty Web Server"),
    
    /**
     * Dubbo 服务提供者/消费者线程池
     */
    DUBBO("dubbo", "Dubbo RPC Framework"),
    
    /**
     * Hikari 数据库连接池
     */
    HIKARI("hikari", "Hikari Connection Pool"),
    
    /**
     * gRPC 线程池
     */
    GRPC("grpc", "gRPC Framework");
    
    private final String code;
    private final String description;
    
    ThirdPartyPoolType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
