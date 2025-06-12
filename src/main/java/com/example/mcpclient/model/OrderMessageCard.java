package com.example.mcpclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单消息卡片模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OrderMessageCard extends MessageCard {
    // 订单特有信息
    private String orderNumber;
    private String orderStatus;
    private LocalDateTime orderTime;
    private double totalAmount;
    private List<OrderItem> items;
    
    // 用户信息
    private Long userId;
    private String userName;
    private String userPhone;
    private String userAddress;
    
    @Override
    public String getType() {
        return "order";
    }
    
    /**
     * 订单项模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productName;
        private String imageUrl;
        private double price;
        private int quantity;
        private Long productId;
        private String productSku;
        private String productCategory;
    }
} 