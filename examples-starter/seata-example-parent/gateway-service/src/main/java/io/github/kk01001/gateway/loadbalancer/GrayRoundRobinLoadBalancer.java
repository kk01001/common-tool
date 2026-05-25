package io.github.archer099.gateway.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author archer099
 * @date 2025-01-08 17:05:00
 * @description 灰度发布负载均衡器
 */
@Slf4j
public class GrayRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final AtomicInteger position;

    public GrayRoundRobinLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.position = new AtomicInteger(ThreadLocalRandom.current().nextInt(1000));
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> processInstanceResponse(supplier, serviceInstances, request));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances, Request request) {
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances, request);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
        if (CollUtil.isEmpty(instances)) {
            log.warn("No instance available for service: " + serviceId);
            return new EmptyResponse();
        }

        // 获取请求头
        String version;
        if (request != null && request.getContext() instanceof RequestDataContext) {
            RequestDataContext context = (RequestDataContext) request.getContext();
            if (context.getClientRequest() != null) {
                List<String> headers = Objects.requireNonNull(context.getClientRequest().getHeaders()).get("version");
                if (CollUtil.isNotEmpty(headers)) {
                    version = headers.getFirst();
                } else {
                    version = null;
                }
            } else {
                version = null;
            }
        } else {
            version = null;
        }

        List<ServiceInstance> targetInstances = instances;
        // 如果请求头中有version，则进行过滤
        if (StrUtil.isNotBlank(version)) {
            List<ServiceInstance> grayInstances = instances.stream()
                    .filter(instance -> {
                        Map<String, String> metadata = instance.getMetadata();
                        return metadata != null && StrUtil.equals(version, metadata.get("version"));
                    })
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(grayInstances)) {
                targetInstances = grayInstances;
                log.debug("Gray routing triggered for service: {}, version: {}, instances count: {}", serviceId, version, targetInstances.size());
            } else {
                log.debug("No gray instance found for service: {}, version: {}, fallback to all instances", serviceId, version);
            }
        }

        if (CollUtil.isEmpty(targetInstances)) {
            return new EmptyResponse();
        }

        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = targetInstances.get(pos % targetInstances.size());

        return new DefaultResponse(instance);
    }
}
