package com.example.mcpclient.service;

import com.example.mcpclient.model.LogisticsMessageCard;
import com.example.mcpclient.model.LogisticsTrackingCard;
import com.example.mcpclient.model.MessageCard;
import com.example.mcpclient.model.OrderMessageCard;
import com.example.mcpclient.model.LogisticsTrackingCard.TrackingDetail;
import com.example.mcpclient.model.OrderMessageCard.OrderItem;
import lombok.extern.slf4j.Slf4j;
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
     * 检测消息文本中是否包含订单、物流相关内容，并创建相应的卡片
     * 
     * 注意：此方法保留是为了向后兼容，新代码应直接调用 createOrderCardFromMessage 或 createLogisticsCardFromMessage 等方法
     */
    public MessageCard detectCardFromMessage(String message) {
        // 简单的关键词检测逻辑
        message = message.toLowerCase();
        
        // 检测订单信息
        if (message.contains("订单") || message.contains("查询订单") || message.contains("订单信息") || 
            message.contains("我的订单") || message.contains("查一下订单")) {
            return createOrderCardFromMessage(message);
        }
        
        // 检测物流信息
        if (message.contains("物流") || message.contains("快递") || message.contains("包裹") || 
            message.contains("查物流") || message.contains("查询物流") || message.contains("查包裹")) {
            if (message.contains("追踪") || message.contains("详情") || message.contains("跟踪") || message.contains("运输状态")) {
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
        String orderNumber = "OD" + System.currentTimeMillis();
        // 尝试使用正则表达式从消息中提取订单号格式 (例如: OD12345678)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)(订单号|订单编号|订单)[:：\\s]*(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find() && matcher.group(2) != null) {
            orderNumber = matcher.group(2);
            // 如果提取的订单号不以OD开头，添加前缀
            if (!orderNumber.toUpperCase().startsWith("OD")) {
                orderNumber = "OD" + orderNumber;
            }
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
        
        // 创建一个简单的订单项
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .productName("智能手表")
                .imageUrl("/images/product-watch.jpg")
                .price(999.0)
                .quantity(1)
                .build());
        
        items.add(OrderItem.builder()
                .productName("蓝牙耳机")
                .imageUrl("/images/product-headphones.jpg")
                .price(499.0)
                .quantity(1)
                .build());
        
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
                .build();
    }
    
    /**
     * 从消息文本创建物流卡片
     */
    public LogisticsMessageCard createLogisticsCardFromMessage(String message) {
        // 尝试从消息中提取快递单号
        String trackingNumber = "SF" + System.currentTimeMillis();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)(快递单号|物流单号|单号|运单号)[:：\\s]*(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find() && matcher.group(2) != null) {
            trackingNumber = matcher.group(2);
        }
        
        // 从消息中提取快递公司
        String courierCompany = "顺丰速运";
        if (message.contains("顺丰")) {
            courierCompany = "顺丰速运";
        } else if (message.contains("圆通")) {
            courierCompany = "圆通快递";
        } else if (message.contains("中通")) {
            courierCompany = "中通快递";
        } else if (message.contains("申通")) {
            courierCompany = "申通快递";
        } else if (message.contains("韵达")) {
            courierCompany = "韵达快递";
        } else if (message.contains("邮政")) {
            courierCompany = "中国邮政";
        } else if (message.contains("京东")) {
            courierCompany = "京东物流";
        }
        
        // 获取物流状态
        String status = "未知";
        if (message.contains("已发出") || message.contains("已发货")) {
            status = "已发出";
        } else if (message.contains("运输中") || message.contains("在途中")) {
            status = "运输中";
        } else if (message.contains("已签收") || message.contains("已收到")) {
            status = "已签收";
        } else if (message.contains("派送中") || message.contains("配送中") || message.contains("派件中")) {
            status = "派送中";
        } else if (message.contains("已揽收") || message.contains("已收件")) {
            status = "已揽收";
        }
        
        // 根据物流状态设置最新更新
        String latestUpdate;
        switch (status) {
            case "已发出":
                latestUpdate = "您的包裹已由卖家发出";
                break;
            case "运输中":
                latestUpdate = "您的包裹正在运输中";
                break;
            case "已签收":
                latestUpdate = "您的包裹已签收";
                break;
            case "派送中":
                latestUpdate = "您的包裹正在派送中，请保持电话畅通";
                break;
            case "已揽收":
                latestUpdate = "您的包裹已被快递员揽收";
                break;
            default:
                latestUpdate = "暂无更新";
                break;
        }
        
        String orderNumber = "OD" + System.currentTimeMillis();
        pattern = java.util.regex.Pattern.compile("(?i)(订单号|订单编号|订单)[:：\\s]*(\\w+)");
        matcher = pattern.matcher(message);
        if (matcher.find() && matcher.group(2) != null) {
            orderNumber = matcher.group(2);
            if (!orderNumber.toUpperCase().startsWith("OD")) {
                orderNumber = "OD" + orderNumber;
            }
        }
        
        return LogisticsMessageCard.builder()
                .id(UUID.randomUUID().toString())
                .title(courierCompany + "物流信息")
                .description("包裹" + status + "，" + latestUpdate)
                .iconUrl("/images/logistics-icon.png")
                .actionUrl("/logistics/" + trackingNumber)
                .createdTime(LocalDateTime.now())
                .courierCompany(courierCompany)
                .courierLogo("/images/courier-" + getCourierCode(courierCompany) + ".png")
                .orderNumber(orderNumber)
                .trackingNumber(trackingNumber)
                .status(status)
                .latestUpdate(latestUpdate)
                .estimatedDeliveryTime(calculateEstimatedDeliveryTime(status))
                .build();
    }
    
    /**
     * 根据快递公司获取代码
     */
    private String getCourierCode(String courierCompany) {
        switch (courierCompany) {
            case "顺丰速运": return "sf";
            case "圆通快递": return "yt";
            case "中通快递": return "zt";
            case "申通快递": return "st";
            case "韵达快递": return "yd";
            case "中国邮政": return "ems";
            case "京东物流": return "jd";
            default: return "sf";
        }
    }
    
    /**
     * 根据物流状态计算预计送达时间
     */
    private LocalDateTime calculateEstimatedDeliveryTime(String status) {
        switch (status) {
            case "已发出":
                return LocalDateTime.now().plus(3, ChronoUnit.DAYS);
            case "运输中":
                return LocalDateTime.now().plus(2, ChronoUnit.DAYS);
            case "派送中":
                return LocalDateTime.now().plus(1, ChronoUnit.DAYS);
            case "已揽收":
                return LocalDateTime.now().plus(3, ChronoUnit.DAYS);
            case "已签收":
                return null;
            default:
                return LocalDateTime.now().plus(3, ChronoUnit.DAYS);
        }
    }
    
    /**
     * 从消息文本创建物流追踪卡片
     */
    public LogisticsTrackingCard createTrackingCardFromMessage(String message) {
        // 提取快递单号
        String trackingNumber = "SF" + System.currentTimeMillis();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)(快递单号|物流单号|单号|运单号)[:：\\s]*(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find() && matcher.group(2) != null) {
            trackingNumber = matcher.group(2);
        }
        
        // 从消息中提取快递公司
        String courierCompany = "顺丰速运";
        if (message.contains("顺丰")) {
            courierCompany = "顺丰速运";
        } else if (message.contains("圆通")) {
            courierCompany = "圆通快递";
        } else if (message.contains("中通")) {
            courierCompany = "中通快递";
        } else if (message.contains("申通")) {
            courierCompany = "申通快递";
        } else if (message.contains("韵达")) {
            courierCompany = "韵达快递";
        } else if (message.contains("邮政")) {
            courierCompany = "中国邮政";
        } else if (message.contains("京东")) {
            courierCompany = "京东物流";
        }
        
        // 确定起始地和目的地
        String originLocation = "广州";
        String destinationLocation = "北京";
        
        // 如果消息中包含城市信息，尝试提取
        pattern = java.util.regex.Pattern.compile("(?:从|自)([\u4e00-\u9fa5]+)(?:到|至|往)([\u4e00-\u9fa5]+)");
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            if (matcher.group(1) != null) originLocation = matcher.group(1);
            if (matcher.group(2) != null) destinationLocation = matcher.group(2);
        }
        
        // 创建物流追踪记录
        List<TrackingDetail> trackingDetails = new ArrayList<>();
        
        // 尝试判断当前物流状态
        String currentStatus = "运输中";
        if (message.contains("已签收") || message.contains("已送达")) {
            currentStatus = "已签收";
        } else if (message.contains("派送中") || message.contains("配送中") || message.contains("派件中")) {
            currentStatus = "派送中";
        } else if (message.contains("已发货") || message.contains("已发出")) {
            currentStatus = "已发货";
        } else if (message.contains("已下单") || message.contains("待发货")) {
            currentStatus = "已下单";
        }
        
        // 根据当前状态生成不同的追踪详情
        int completionPercentage = 0;
        switch (currentStatus) {
            case "已签收":
                completionPercentage = 100;
                trackingDetails.add(TrackingDetail.builder()
                        .status("已签收")
                        .description("包裹已签收，签收人：本人")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.HOURS))
                        .location(destinationLocation)
                        .current(true)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("派送中")
                        .description("快递员正在派送，请保持电话畅通")
                        .timestamp(LocalDateTime.now().minus(3, ChronoUnit.HOURS))
                        .location(destinationLocation)
                        .current(false)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("到达目的地")
                        .description("包裹已到达" + destinationLocation + "转运中心")
                        .timestamp(LocalDateTime.now().minus(10, ChronoUnit.HOURS))
                        .location(destinationLocation)
                        .current(false)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("运输中")
                        .description("包裹正在运输中")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
                        .location("中转站")
                        .current(false)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("已发货")
                        .description("卖家已发货")
                        .timestamp(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                        .location(originLocation)
                        .current(false)
                        .build());
                break;
                
            case "派送中":
                completionPercentage = 80;
                trackingDetails.add(TrackingDetail.builder()
                        .status("派送中")
                        .description("快递员正在派送，请保持电话畅通")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.HOURS))
                        .location(destinationLocation)
                        .current(true)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("到达目的地")
                        .description("包裹已到达" + destinationLocation + "转运中心")
                        .timestamp(LocalDateTime.now().minus(8, ChronoUnit.HOURS))
                        .location(destinationLocation)
                        .current(false)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("运输中")
                        .description("包裹正在运输中")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
                        .location("中转站")
                        .current(false)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("已发货")
                        .description("卖家已发货")
                        .timestamp(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                        .location(originLocation)
                        .current(false)
                        .build());
                break;
                
            case "运输中":
                completionPercentage = 50;
                trackingDetails.add(TrackingDetail.builder()
                        .status("运输中")
                        .description("包裹正在从" + originLocation + "发往" + destinationLocation)
                        .timestamp(LocalDateTime.now().minus(12, ChronoUnit.HOURS))
                        .location("中转站")
                        .current(true)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("已揽收")
                        .description("快递员已揽收")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS))
                        .location(originLocation)
                        .current(false)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("已发货")
                        .description("卖家已发货")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS).minus(2, ChronoUnit.HOURS))
                        .location(originLocation)
                        .current(false)
                        .build());
                break;
                
            case "已发货":
                completionPercentage = 20;
                trackingDetails.add(TrackingDetail.builder()
                        .status("已揽收")
                        .description("快递员已揽收")
                        .timestamp(LocalDateTime.now().minus(2, ChronoUnit.HOURS))
                        .location(originLocation)
                        .current(true)
                        .build());
                trackingDetails.add(TrackingDetail.builder()
                        .status("已发货")
                        .description("卖家已发货")
                        .timestamp(LocalDateTime.now().minus(4, ChronoUnit.HOURS))
                        .location(originLocation)
                        .current(false)
                        .build());
                break;
                
            case "已下单":
                completionPercentage = 10;
                trackingDetails.add(TrackingDetail.builder()
                        .status("已下单")
                        .description("订单已生成，等待卖家发货")
                        .timestamp(LocalDateTime.now().minus(1, ChronoUnit.HOURS))
                        .location("系统")
                        .current(true)
                        .build());
                break;
        }
        
        // 提取订单号
        String orderNumber = "OD" + System.currentTimeMillis();
        pattern = java.util.regex.Pattern.compile("(?i)(订单号|订单编号|订单)[:：\\s]*(\\w+)");
        matcher = pattern.matcher(message);
        if (matcher.find() && matcher.group(2) != null) {
            orderNumber = matcher.group(2);
            if (!orderNumber.toUpperCase().startsWith("OD")) {
                orderNumber = "OD" + orderNumber;
            }
        }
        
        return LogisticsTrackingCard.builder()
                .id(UUID.randomUUID().toString())
                .title(courierCompany + "物流追踪")
                .description("订单 " + orderNumber + " 的物流追踪信息")
                .iconUrl("/images/tracking-icon.png")
                .actionUrl("/tracking/" + trackingNumber)
                .createdTime(LocalDateTime.now())
                .courierCompany(courierCompany)
                .courierLogo("/images/courier-" + getCourierCode(courierCompany) + ".png")
                .orderNumber(orderNumber)
                .trackingNumber(trackingNumber)
                .currentStatus(currentStatus)
                .originLocation(originLocation)
                .destinationLocation(destinationLocation)
                .estimatedDistance(getEstimatedDistance(originLocation, destinationLocation))
                .completionPercentage(completionPercentage)
                .estimatedDeliveryTime(calculateEstimatedDeliveryTime(currentStatus))
                .trackingDetails(trackingDetails)
                .build();
    }
    
    /**
     * 估算两地之间的距离（简化处理）
     */
    private double getEstimatedDistance(String origin, String destination) {
        // 这里使用固定的距离，实际应用中可以通过地图API计算
        if ((origin.equals("广州") && destination.equals("北京")) || 
            (origin.equals("北京") && destination.equals("广州"))) {
            return 1897.5;
        } else if ((origin.equals("上海") && destination.equals("北京")) || 
                  (origin.equals("北京") && destination.equals("上海"))) {
            return 1067.9;
        } else if ((origin.equals("广州") && destination.equals("上海")) || 
                  (origin.equals("上海") && destination.equals("广州"))) {
            return 1213.0;
        } else {
            // 默认距离
            return 1000.0;
        }
    }
} 