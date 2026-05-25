package io.github.archer099.redis.examples.controller;

import io.github.archer099.redis.examples.service.SerializationTestService;
import io.github.archer099.redis.examples.vo.SerializationResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Serialization test controller
 * 
 * @author archer099
 */
@RestController
@RequestMapping("/api/serialization")
public class SerializationTestController {
    
    private final SerializationTestService serializationTestService;
    
    public SerializationTestController(SerializationTestService serializationTestService) {
        this.serializationTestService = serializationTestService;
    }
    
    /**
     * Test all serialization methods and compare results
     */
    @GetMapping("/test")
    public List<SerializationResultVO> testAllSerializations(
            @RequestParam(defaultValue = "1000") int iterations) {
        return serializationTestService.testAllSerializations(iterations);
    }
}

