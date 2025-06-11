package com.example.mcpclient.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 物流消息卡片模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class LogisticsMessageCard extends MessageCard {
    // 物流特有信息
    private String courierCompany;
    private String courierLogo;
    private String orderNumber;
    private String trackingNumber;
    private String status;
    private String latestUpdate;
    private LocalDateTime estimatedDeliveryTime;
    
    @Override
    public String getType() {
        return "logistics";
    }
} 