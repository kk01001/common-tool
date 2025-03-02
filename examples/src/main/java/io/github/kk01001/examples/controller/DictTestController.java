package io.github.kk01001.examples.controller;

import io.github.kk01001.dict.DictCache;
import io.github.kk01001.examples.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dict")
public class DictTestController {
    
    /**
     * 测试字典翻译
     */
    @GetMapping("/test")
    public List<Order> test() {
        List<Order> orders = new ArrayList<>();
        
        Order order1 = new Order();
        order1.setId("1");
        order1.setStatus("1");
        order1.setPayType("1");
        order1.setAmount(100.0);
        orders.add(order1);
        
        Order order2 = new Order();
        order2.setId("2");
        order2.setStatus("2");
        order2.setPayType("2");
        order2.setAmount(200.0);
        orders.add(order2);
        
        return orders;
    }
    
    /**
     * 刷新字典缓存
     */
    @PostMapping("/refresh")
    public void refresh(@RequestParam String type, @RequestBody Map<String, String> dict) {
        log.info("Refreshing dictionary cache for type: {}, data: {}", type, dict);
        DictCache.refresh(type, dict);
    }
    
    /**
     * 刷新所有字典缓存
     */
    @PostMapping("/refresh/all")
    public void refreshAll(@RequestBody Map<String, Map<String, String>> allDict) {
        log.info("Refreshing all dictionary cache, data: {}", allDict);
        DictCache.refreshAll();
    }
} 