package io.github.kk01001.netty.registry;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class EndpointMethodHandler {
    private final Object bean;
    private Method onOpenMethod;
    private Method onMessageMethod;
    private Method onBinaryMessageMethod;
    private Method onCloseMethod;
    private Method onErrorMethod;

}