package io.github.kk01001.examples.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {
    
    @GetMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public String chat() {
        return "forward:/chat.html";
    }

    @GetMapping(value = "/chat2", produces = "text/html;charset=UTF-8")
    public String chat2() {
        return "forward:/chat2.html";
    }
} 