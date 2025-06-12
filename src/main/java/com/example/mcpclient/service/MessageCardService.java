package com.example.mcpclient.service;

import com.example.mcpclient.model.LogisticsMessageCard;
import com.example.mcpclient.model.LogisticsTrackingCard;
import com.example.mcpclient.model.MessageCard;
import com.example.mcpclient.model.OrderMessageCard;
import com.example.mcpclient.model.LogisticsTrackingCard.TrackingDetail;
import com.example.mcpclient.model.OrderMessageCard.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 消息卡片服务，负责创建、存储和检索消息卡片
 */
@Service
@Slf4j
public class MessageCardService {
    
    // 存储所有消息卡片的内存数据库
    private final Map<String, MessageCard> cardDatabase = new ConcurrentHashMap<>();
    
    @Autowired
    private OrderDataService orderDataService;
    
    @Autowired
    private UserDataService userDataService;
    
    /**
     * 获取指定ID的消息卡片
     * 
     * @param cardId 卡片ID
     * @return 消息卡片，如果不存在则返回null
     */
    public MessageCard getCardById(String cardId) {
        return cardDatabase.get(cardId);
    }
    
    /**
     * 获取所有消息卡片
     * 
     * @return 所有消息卡片列表
     */
    public List<MessageCard> getAllCards() {
        return new ArrayList<>(cardDatabase.values());
    }
    
    /**
     * 按类型获取消息卡片
     * 
     * @param type 卡片类型
     * @return 指定类型的卡片列表
     */
    public List<MessageCard> getCardsByType(String type) {
        return cardDatabase.values().stream()
                .filter(card -> card.getType().equals(type))
                .collect(Collectors.toList());
    }
    
    /**
     * 保存或更新消息卡片
     * 
     * @param card 要保存的卡片
     * @return 保存后的卡片
     */
    public MessageCard saveCard(MessageCard card) {
        // 如果没有ID，生成一个新ID
        if (card.getId() == null || card.getId().isEmpty()) {
            card.setId(UUID.randomUUID().toString());
        }
        
        // 设置创建时间（如果为空）
        if (card.getCreatedTime() == null) {
            card.setCreatedTime(LocalDateTime.now());
        }
        
        cardDatabase.put(card.getId(), card);
        log.debug("Saved message card: {}", card.getId());
        return card;
    }
    
    /**
     * 删除消息卡片
     * 
     * @param cardId 要删除的卡片ID
     * @return 是否删除成功
     */
    public boolean deleteCard(String cardId) {
        MessageCard removed = cardDatabase.remove(cardId);
        if (removed != null) {
            log.debug("Deleted message card: {}", cardId);
            return true;
        }
        return false;
    }
    
    /**
     * 创建示例订单消息卡片
     * 
     * @return 创建的订单消息卡片
     */
    public OrderMessageCard createSampleOrderCard() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .productName("智能手表")
                .imageUrl("/images/product1.jpg")
                .price(199.99)
                .quantity(1)
                .build());
        items.add(OrderItem.builder()
                .productName("蓝牙耳机")
                .imageUrl("/images/product2.jpg")
                .price(100.00)
                .quantity(1)
                .build());
        
        OrderMessageCard card = OrderMessageCard.builder()
                .id(UUID.randomUUID().toString())
                .title("您的订单已发货")
                .description("订单 #12345678 已于今天开始配送")
                .createdTime(LocalDateTime.now())
                .iconUrl("/images/order-icon.png")
                .actionUrl("/orders/12345678")
                .orderNumber("12345678")
                .orderStatus("已发货")
                .orderTime(LocalDateTime.now().minusDays(1))
                .totalAmount(299.99)
                .items(items)
                .build();
        
        saveCard(card);
        return card;
    }
    
    /**
     * 创建示例物流消息卡片
     * 
     * @return 创建的物流消息卡片
     */
    public LogisticsMessageCard createSampleLogisticsCard() {
        LogisticsMessageCard card = LogisticsMessageCard.builder()
                .id(UUID.randomUUID().toString())
                .title("您的包裹正在配送中")
                .description("您的包裹预计将于明天送达")
                .createdTime(LocalDateTime.now())
                .iconUrl("/images/logistics-icon.png")
                .actionUrl("/logistics/SF1234567890")
                .orderNumber("12345678")
                .trackingNumber("SF1234567890")
                .courierCompany("顺丰速运")
                .courierLogo("/images/sf-logo.png")
                .status("运输中")
                .latestUpdate("包裹已到达北京分拣中心")
                .estimatedDeliveryTime(LocalDateTime.now().plusDays(1))
                .build();
        
        saveCard(card);
        return card;
    }
    
    /**
     * 创建示例物流追踪卡片
     * 
     * @return 创建的物流追踪卡片
     */
    public LogisticsTrackingCard createSampleTrackingCard() {
        List<TrackingDetail> details = new ArrayList<>();
        details.add(TrackingDetail.builder()
                .status("到达分拣中心")
                .description("包裹已到达北京分拣中心")
                .timestamp(LocalDateTime.now().minusHours(2))
                .location("北京市顺义区")
                .current(true)
                .build());
        details.add(TrackingDetail.builder()
                .status("运输中")
                .description("包裹已从广州发往北京")
                .timestamp(LocalDateTime.now().minusHours(8))
                .location("广州市白云区")
                .current(false)
                .build());
        details.add(TrackingDetail.builder()
                .status("已发货")
                .description("卖家已发货")
                .timestamp(LocalDateTime.now().minusHours(12))
                .location("广州市")
                .current(false)
                .build());
        details.add(TrackingDetail.builder()
                .status("已下单")
                .description("订单已生成")
                .timestamp(LocalDateTime.now().minusDays(1))
                .location("系统")
                .current(false)
                .build());
        
        LogisticsTrackingCard card = LogisticsTrackingCard.builder()
                .id(UUID.randomUUID().toString())
                .title("物流追踪详情")
                .description("订单 #12345678 的物流追踪信息")
                .createdTime(LocalDateTime.now())
                .iconUrl("/images/tracking-icon.png")
                .actionUrl("/tracking/SF1234567890")
                .orderNumber("12345678")
                .trackingNumber("SF1234567890")
                .courierCompany("顺丰速运")
                .courierLogo("/images/sf-logo.png")
                .currentStatus("运输中")
                .originLocation("广州")
                .destinationLocation("北京")
                .estimatedDistance(1897.5)
                .completionPercentage(60)
                .estimatedDeliveryTime(LocalDateTime.now().plusDays(1))
                .trackingDetails(details)
                .build();
        
        saveCard(card);
        return card;
    }
    
    /**
     * 初始化示例卡片数据
     */
    public void initSampleCards() {
        if (cardDatabase.isEmpty()) {
            createSampleOrderCard();
            createSampleLogisticsCard();
            createSampleTrackingCard();
            log.info("Initialized sample message cards");
        }
    }

    /**
     * 添加卡片
     */
    public MessageCard addCard(MessageCard card) {
        return saveCard(card);
    }
    
    /**
     * 检测消息文本中是否包含订单、物流相关内容
     */
    public MessageCard detectCardFromMessage(String message) {
        // 简单的关键词检测逻辑
        message = message.toLowerCase();
        
        // 检测订单信息
        if (message.contains("订单") && (message.contains("已发货") || message.contains("待付款") || message.contains("已付款"))) {
            return createOrderCardFromMessage(message);
        }
        
        // 检测物流信息
        if (message.contains("物流") || message.contains("快递") || message.contains("包裹")) {
            if (message.contains("追踪") || message.contains("详情") || message.contains("跟踪")) {
                return createTrackingCardFromMessage(message);
            } else {
                return createLogisticsCardFromMessage(message);
            }
        }
        
        return null;
    }
    
    /**
     * 从消息文本创建订单卡片
     */
    public OrderMessageCard createOrderCardFromMessage(String message) {
        // 尝试从消息中提取订单号
        String orderNumber = null;
        boolean specificOrderRequested = false;
        
        // 尝试使用正则表达式从消息中提取订单号格式 (例如: OD12345678)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)(订单号|订单编号|订单)[:：\\s]*(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find() && matcher.group(2) != null) {
            orderNumber = matcher.group(2);
            specificOrderRequested = true;
            
            // 如果提取的订单号不以OD开头，添加前缀
            if (!orderNumber.toUpperCase().startsWith("OD")) {
                orderNumber = "OD" + orderNumber;
            }
            
            // 尝试从MCP服务获取订单信息
            com.fasterxml.jackson.databind.JsonNode orderData = orderDataService.getOrderByOrderNo(orderNumber);
            if (orderData != null) {
                log.info("从MCP服务获取到订单信息: {}", orderNumber);
                return createOrderCardFromData(orderData);
            }
            
            // 这里模拟数据库查询：如果订单号包含"404"、"不存在"或"unknown"，表示订单不存在
            if (orderNumber.toLowerCase().contains("404") || 
                orderNumber.toLowerCase().contains("不存在") || 
                orderNumber.toLowerCase().contains("unknown")) {
                log.info("Specific order requested but not found: {}", orderNumber);
                return null;
            }
        } else {
            // 如果没有指定订单号，生成一个随机订单号
            orderNumber = "OD" + System.currentTimeMillis();
        }
        
        // 检查消息中是否明确要求查询不存在的订单
        if (message.contains("不存在的订单") || message.contains("未找到") || message.contains("找不到")) {
            log.info("User explicitly asked for non-existent order");
            return null;
        }
        
        // 检测订单状态
        String orderStatus = "未知";
        if (message.contains("待付款") || message.contains("未付款")) {
            orderStatus = "待付款";
        } else if (message.contains("已发货") || message.contains("配送中") || message.contains("运输中")) {
            orderStatus = "已发货";
        } else if (message.contains("已付款") || message.contains("已支付") || message.contains("已下单")) {
            orderStatus = "已付款";
        } else if (message.contains("已完成") || message.contains("已签收")) {
            orderStatus = "已完成";
        } else if (message.contains("已取消")) {
            orderStatus = "已取消";
        }
        
        // 创建一个示例订单项
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .productName("智能手表")
                .imageUrl("/images/product-watch.jpg")
                .price(999.0)
                .quantity(1)
                .productId(1001L)
                .productSku("WATCH-2023")
                .productCategory("电子产品")
                .build());
        
        items.add(OrderItem.builder()
                .productName("蓝牙耳机")
                .imageUrl("/images/product-headphones.jpg")
                .price(499.0)
                .quantity(1)
                .productId(1002L)
                .productSku("HP-2023")
                .productCategory("电子产品")
                .build());
        
        // 创建一个模拟用户信息
        Long userId = 10001L;
        String userName = "张三";
        String userPhone = "135****6789";
        String userAddress = "北京市海淀区中关村大街1号";
        
        return OrderMessageCard.builder()
                .id(UUID.randomUUID().toString())
                .title("订单详情")
                .description("订单" + orderNumber + "状态：" + orderStatus)
                .iconUrl("/images/order-icon.png")
                .actionUrl("/order/" + orderNumber)
                .createdTime(LocalDateTime.now())
                .orderNumber(orderNumber)
                .orderStatus(orderStatus)
                .orderTime(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
                .totalAmount(1498.0)
                .items(items)
                .userId(userId)
                .userName(userName)
                .userPhone(userPhone)
                .userAddress(userAddress)
                .build();
    }
    
    /**
     * 从消息文本创建物流卡片
     */
    private LogisticsMessageCard createLogisticsCardFromMessage(String message) {
        String status = "未知";
        if (message.contains("已发出")) {
            status = "已发出";
        } else if (message.contains("运输中")) {
            status = "运输中";
        } else if (message.contains("已签收")) {
            status = "已签收";
        } else if (message.contains("派送中")) {
            status = "派送中";
        }
        
        return LogisticsMessageCard.builder()
                .id(UUID.randomUUID().toString())
                .title("物流" + status)
                .description("您的包裹" + status)
                .iconUrl("/images/logistics-icon.png")
                .actionUrl("/logistics/detail")
                .createdTime(LocalDateTime.now())
                .courierCompany("顺丰速运")
                .courierLogo("/images/courier-sf.png")
                .orderNumber("OD" + System.currentTimeMillis())
                .trackingNumber("SF" + System.currentTimeMillis())
                .status(status)
                .latestUpdate("包裹" + status)
                .estimatedDeliveryTime(LocalDateTime.now().plus(2, ChronoUnit.DAYS))
                .build();
    }
    
    /**
     * 从消息文本创建物流追踪卡片
     */
    private LogisticsTrackingCard createTrackingCardFromMessage(String message) {
        // 创建物流追踪记录
        List<TrackingDetail> trackingDetails = new ArrayList<>();
        trackingDetails.add(TrackingDetail.builder()
                .status("已揽收")
                .description("快递员已揽收")
                .timestamp(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                .location("发货地")
                .current(false)
                .build());
        trackingDetails.add(TrackingDetail.builder()
                .status("运输中")
                .description("包裹正在运输中")
                .timestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
                .location("中转站")
                .current(true)
                .build());
        
        return LogisticsTrackingCard.builder()
                .id(UUID.randomUUID().toString())
                .title("物流追踪详情")
                .description("包裹正在运输中")
                .iconUrl("/images/tracking-icon.png")
                .actionUrl("/logistics/tracking")
                .createdTime(LocalDateTime.now())
                .courierCompany("顺丰速运")
                .courierLogo("/images/courier-sf.png")
                .orderNumber("OD" + System.currentTimeMillis())
                .trackingNumber("SF" + System.currentTimeMillis())
                .originLocation("发货地")
                .destinationLocation("收货地")
                .completionPercentage(50)
                .trackingDetails(trackingDetails)
                .build();
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(int statusCode) {
        switch (statusCode) {
            case 0: return "待付款";
            case 1: return "已付款";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 从API返回的订单数据创建订单卡片
     */
    private OrderMessageCard createOrderCardFromData(com.fasterxml.jackson.databind.JsonNode orderData) {
        try {
            String orderNumber = orderData.path("orderNo").asText();
            Long orderId = orderData.path("id").asLong();
            Long userId = orderData.path("userId").asLong();
            
            // 获取订单状态
            int statusCode = orderData.path("status").asInt(0);
            String orderStatus = getOrderStatusText(statusCode);
            
            // 获取订单时间
            String orderTimeStr = orderData.path("createdAt").asText();
            LocalDateTime orderTime;
            try {
                if (orderTimeStr.isEmpty()) {
                    orderTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
                } else if (orderTimeStr.contains("T")) {
                    orderTime = LocalDateTime.parse(orderTimeStr);
                } else {
                    // 将日期时间格式如 "2023-06-01 12:34:56" 转换为 "2023-06-01T12:34:56"
                    orderTime = LocalDateTime.parse(orderTimeStr.replace(" ", "T"));
                }
            } catch (Exception e) {
                log.warn("解析订单时间出错: {}", orderTimeStr, e);
                orderTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
            }
            
            // 获取订单金额
            double totalAmount = orderData.path("amount").asDouble(0.0);
            
            // 获取用户信息
            com.fasterxml.jackson.databind.JsonNode userData = userDataService.getUserById(userId);
            String userName = "用户";
            String userPhone = "";
            String userAddress = orderData.path("address").asText("");
            
            if (userData != null) {
                userName = userData.path("username").asText("用户");
                userPhone = userData.path("phone").asText("");
                if (userPhone.length() > 7) {
                    // 手机号脱敏处理
                    userPhone = userPhone.substring(0, 3) + "****" + userPhone.substring(7);
                }
            }
            
            // 创建订单项
            List<OrderItem> items = new ArrayList<>();
            Long itemId = orderData.path("itemId").asLong(0);
            int quantity = orderData.path("quantity").asInt(1);
            
            // 这里应该有一个服务来获取商品信息，暂时用模拟数据
            String productName = "商品";
            String imageUrl = "/images/product-default.jpg";
            double price = totalAmount / quantity;
            String productSku = "SKU-" + itemId;
            String productCategory = "商品";
            
            // 根据商品ID查找不同商品
            if (itemId == 1001 || itemId % 10 == 1) {
                productName = "智能手表";
                imageUrl = "/images/product-watch.jpg";
                productCategory = "电子产品";
            } else if (itemId == 1002 || itemId % 10 == 2) {
                productName = "蓝牙耳机";
                imageUrl = "/images/product-headphones.jpg";
                productCategory = "电子产品";
            } else if (itemId == 1003 || itemId % 10 == 3) {
                productName = "机械键盘";
                imageUrl = "/images/product-keyboard.jpg";
                productCategory = "电子产品";
            } else if (itemId == 1004 || itemId % 10 == 4) {
                productName = "运动鞋";
                imageUrl = "/images/product-shoes.jpg";
                productCategory = "服装";
            } else if (itemId == 1005 || itemId % 10 == 5) {
                productName = "牛仔裤";
                imageUrl = "/images/product-jeans.jpg";
                productCategory = "服装";
            }
            
            items.add(OrderItem.builder()
                    .productName(productName)
                    .imageUrl(imageUrl)
                    .price(price)
                    .quantity(quantity)
                    .productId(itemId)
                    .productSku(productSku)
                    .productCategory(productCategory)
                    .build());
            
            // 构建订单卡片
            return OrderMessageCard.builder()
                    .id(UUID.randomUUID().toString())
                    .title("订单详情")
                    .description("订单" + orderNumber + "状态：" + orderStatus)
                    .iconUrl("/images/order-icon.png")
                    .actionUrl("/order/" + orderNumber)
                    .createdTime(LocalDateTime.now())
                    .orderNumber(orderNumber)
                    .orderStatus(orderStatus)
                    .orderTime(orderTime)
                    .totalAmount(totalAmount)
                    .items(items)
                    .userId(userId)
                    .userName(userName)
                    .userPhone(userPhone)
                    .userAddress(userAddress)
                    .build();
        } catch (Exception e) {
            log.error("从API数据创建订单卡片时出错", e);
            return null;
        }
    }
} 