package com.example.mcpclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 页面控制器，处理卡片页面
 */
@Controller
@RequestMapping("/cards")
public class PageController {
    
    /**
     * 卡片页面
     */
    @GetMapping
    public String cardsPage() {
        return "cards";
    }
} 