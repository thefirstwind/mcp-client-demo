package com.example.mcpclient.controller;

import com.example.mcpclient.model.LogisticsMessageCard;
import com.example.mcpclient.model.LogisticsTrackingCard;
import com.example.mcpclient.model.MessageCard;
import com.example.mcpclient.model.OrderMessageCard;
import com.example.mcpclient.service.MessageCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息卡片控制器，提供卡片相关的API
 */
@RestController
@RequestMapping("/api/cards")
@Slf4j
public class MessageCardController {

    @Autowired
    private MessageCardService messageCardService;
    
    /**
     * 获取所有卡片
     */
    @GetMapping
    public ResponseEntity<List<MessageCard>> getAllCards() {
        return ResponseEntity.ok(messageCardService.getAllCards());
    }
    
    /**
     * 获取指定类型的卡片
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<MessageCard>> getCardsByType(@PathVariable String type) {
        return ResponseEntity.ok(messageCardService.getCardsByType(type));
    }
    
    /**
     * 获取指定ID的卡片
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageCard> getCardById(@PathVariable String id) {
        MessageCard card = messageCardService.getCardById(id);
        if (card != null) {
            return ResponseEntity.ok(card);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 保存卡片
     */
    @PostMapping
    public ResponseEntity<MessageCard> saveCard(@RequestBody MessageCard card) {
        return ResponseEntity.ok(messageCardService.saveCard(card));
    }
    
    /**
     * 删除卡片
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        boolean deleted = messageCardService.deleteCard(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 初始化示例卡片
     */
    @PostMapping("/init")
    public ResponseEntity<String> initSampleCards() {
        messageCardService.initSampleCards();
        return ResponseEntity.ok("Sample cards initialized");
    }
    
    /**
     * 创建订单消息卡片
     */
    @PostMapping("/order")
    public ResponseEntity<MessageCard> createOrderCard(@RequestBody OrderMessageCard card) {
        return ResponseEntity.ok(messageCardService.saveCard(card));
    }
    
    /**
     * 创建物流消息卡片
     */
    @PostMapping("/logistics")
    public ResponseEntity<MessageCard> createLogisticsCard(@RequestBody LogisticsMessageCard card) {
        return ResponseEntity.ok(messageCardService.saveCard(card));
    }
    
    /**
     * 创建物流追记卡片
     */
    @PostMapping("/tracking")
    public ResponseEntity<MessageCard> createTrackingCard(@RequestBody LogisticsTrackingCard card) {
        return ResponseEntity.ok(messageCardService.saveCard(card));
    }
    
    /**
     * 创建示例订单消息卡片
     */
    @PostMapping("/sample/order")
    public ResponseEntity<OrderMessageCard> createSampleOrderCard() {
        return ResponseEntity.ok(messageCardService.createSampleOrderCard());
    }
    
    /**
     * 创建示例物流消息卡片
     */
    @PostMapping("/sample/logistics")
    public ResponseEntity<LogisticsMessageCard> createSampleLogisticsCard() {
        return ResponseEntity.ok(messageCardService.createSampleLogisticsCard());
    }
    
    /**
     * 创建示例物流追记卡片
     */
    @PostMapping("/sample/tracking")
    public ResponseEntity<LogisticsTrackingCard> createSampleTrackingCard() {
        return ResponseEntity.ok(messageCardService.createSampleTrackingCard());
    }
} 