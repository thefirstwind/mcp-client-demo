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
 * 物流追踪卡片模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class LogisticsTrackingCard extends MessageCard {
    /**
     * 订单号
     */
    private String orderNumber;
    
    /**
     * 物流单号
     */
    private String trackingNumber;
    
    /**
     * 物流公司
     */
    private String courierCompany;
    
    /**
     * 物流公司Logo
     */
    private String courierLogo;
    
    /**
     * 当前物流状态
     */
    private String currentStatus;
    
    /**
     * 发货地点
     */
    private String originLocation;
    
    /**
     * 目的地
     */
    private String destinationLocation;
    
    /**
     * 预计总里程（公里）
     */
    private double estimatedDistance;
    
    /**
     * 已完成里程百分比（0-100）
     */
    private int completionPercentage;
    
    /**
     * 物流追踪详情列表
     */
    private List<TrackingDetail> trackingDetails;
    
    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;
    
    @Override
    public String getType() {
        return "tracking";
    }
    
    /**
     * 物流追踪详情模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingDetail {
        private String status;
        private String description;
        private LocalDateTime timestamp;
        private String location;
        private boolean current;
    }
} 