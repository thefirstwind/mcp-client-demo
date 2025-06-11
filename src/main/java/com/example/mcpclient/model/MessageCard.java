package com.example.mcpclient.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 消息卡片基类，包含所有卡片通用的属性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderMessageCard.class, name = "order"),
    @JsonSubTypes.Type(value = LogisticsMessageCard.class, name = "logistics"),
    @JsonSubTypes.Type(value = LogisticsTrackingCard.class, name = "tracking")
})
public abstract class MessageCard {
    /**
     * 卡片ID
     */
    private String id;
    
    /**
     * 卡片标题
     */
    private String title;
    
    /**
     * 卡片描述
     */
    private String description;
    
    /**
     * 卡片图标URL
     */
    private String iconUrl;
    
    /**
     * 卡片操作URL，点击卡片后的跳转地址
     */
    private String actionUrl;
    
    /**
     * 卡片创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 获取卡片类型
     */
    public abstract String getType();
} 