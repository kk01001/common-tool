package io.github.kk01001.examples.dict;

import io.github.kk01001.common.dict.DictLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DbDictLoader implements DictLoader {
    
    @Override
    public Map<String, String> loadDict(String type) {
        log.info("Loading dictionary data for type: {}", type);
        // 模拟从数据库加载字典数据
        Map<String, String> dict = new HashMap<>();
        if ("order_status".equals(type)) {
            dict.put("1", "待支付");
            dict.put("2", "已支付");
            dict.put("3", "已发货");
            dict.put("4", "已完成");
            dict.put("5", "已取消");
        }
        if ("pay_type".equals(type)) {
            dict.put("1", "支付宝");
            dict.put("2", "微信");
            dict.put("3", "银行卡");
        }
        if ("user_status".equals(type)) {
            dict.put("0", "禁用");
            dict.put("1", "正常");
        }
        if ("user_type".equals(type)) {
            dict.put("normal", "普通用户");
            dict.put("vip", "VIP用户");
        }
        return dict;
    }
    
    @Override
    public Map<String, Map<String, String>> loadAllDict() {
        log.info("Loading all dictionary data");
        Map<String, Map<String, String>> allDict = new HashMap<>();

        Map<String, String> orderStatus = new HashMap<>();
        orderStatus.put("1", "待支付");
        orderStatus.put("2", "已支付");
        orderStatus.put("3", "已发货");
        orderStatus.put("4", "已完成");
        orderStatus.put("5", "已取消");
        allDict.put("order_status", orderStatus);

        Map<String, String> userStatus = new HashMap<>();
        userStatus.put("0", "禁用");
        userStatus.put("1", "正常");
        allDict.put("user_status", userStatus);

        Map<String, String> userType = new HashMap<>();
        userType.put("normal", "普通用户");
        userType.put("vip", "VIP用户");
        allDict.put("user_type", userType);

        Map<String, String> payType = new HashMap<>();
        payType.put("1", "支付宝");
        payType.put("2", "微信");
        payType.put("3", "银行卡");
        allDict.put("pay_type", payType);

        return allDict;
    }
} 