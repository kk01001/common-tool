package io.github.kk01001.mybatis.core;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * date 2023-07-22 15:15:00
 */
public class RequestDataHelper {

    /**
     * 请求参数存取
     */
    private static final ThreadLocal<Map<String, Object>> REQUEST_DATA = new TransmittableThreadLocal<>();

    /**
     * 设置请求参数
     *
     * @param requestData 请求参数 MAP 对象
     */
    public static void setRequestData(Map<String, Object> requestData) {
        REQUEST_DATA.set(requestData);
    }

    public static void setRequestData(String param, Object value) {
        Map<String, Object> dataMap = getRequestData();
        if (CollectionUtils.isEmpty(dataMap)) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put(param, value);
            setRequestData(requestData);
            return;
        }
        dataMap.put(param, value);
        setRequestData(dataMap);
    }

    /**
     * 获取请求参数
     *
     * @param param 请求参数
     * @return 请求参数 MAP 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRequestData(String param) {
        Map<String, Object> dataMap = getRequestData();
        if (CollectionUtils.isNotEmpty(dataMap)) {
            return (T) dataMap.get(param);
        }
        return null;
    }

    /**
     * 获取请求参数
     *
     * @return 请求参数 MAP 对象
     */
    public static Map<String, Object> getRequestData() {
        return REQUEST_DATA.get();
    }

    public static void remove() {
        REQUEST_DATA.remove();
    }

}
