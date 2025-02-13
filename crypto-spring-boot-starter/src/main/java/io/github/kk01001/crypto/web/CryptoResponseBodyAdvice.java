package io.github.kk01001.crypto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.ParamsCrypto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class CryptoResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ParamsCryptoProvider cryptoProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ParamsCrypto.class);
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        ParamsCrypto crypto = returnType.getMethodAnnotation(ParamsCrypto.class);
        if (crypto == null || !crypto.responseEncrypt() || body == null) {
            return body;
        }

        String content = objectMapper.writeValueAsString(body);
        return cryptoProvider.encrypt(content);
    }
} 