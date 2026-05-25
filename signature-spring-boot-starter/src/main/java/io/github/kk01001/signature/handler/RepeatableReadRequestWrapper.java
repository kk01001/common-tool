package io.github.archer099.signature.handler;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 可重复读取请求体的 HttpServletRequest 包装器
 */
public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {
    
    /**
     * 缓存的请求体
     */
    private final byte[] body;
    
    public RepeatableReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = IoUtil.readBytes(request.getInputStream());
    }
    
    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // 不需要实现
            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }
    
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
    
    /**
     * 获取请求体字符串
     */
    public String getBody() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
