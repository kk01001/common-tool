package io.github.kk01001.crypto.web;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.ParamsCrypto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
@RequiredArgsConstructor
public class CryptoRequestBodyAdvice implements RequestBodyAdvice {

    private final ParamsCryptoProvider cryptoProvider;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(ParamsCrypto.class);
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        ParamsCrypto crypto = parameter.getMethodAnnotation(ParamsCrypto.class);
        if (crypto == null || !crypto.requestDecrypt()) {
            return inputMessage;
        }
        return new DecryptHttpInputMessage(inputMessage, cryptoProvider);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @RequiredArgsConstructor
    static class DecryptHttpInputMessage implements HttpInputMessage {

        private final HttpInputMessage inputMessage;

        private final ParamsCryptoProvider cryptoProvider;

        @Override
        public InputStream getBody() throws IOException {
            String content = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
            String decryptContent = cryptoProvider.decrypt(content);
            return new ByteArrayInputStream(decryptContent.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return inputMessage.getHeaders();
        }
    }
} 