package com.javalearn.advancedtool.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单工具类 - 演示 @Tool 注解的高级用法
 * <p>
 * 知识点：
 * 1. @Tool 注解声明式定义工具
 * 2. @ToolParam 注解描述参数
 * 3. ToolContext 接收运行时上下文
 * 4. returnDirect = true 直接返回结果给用户
 */
public class OrderTools {

    private static final Map<String, Order> ORDER_DB = new HashMap<>();

    static {
        ORDER_DB.put("ORD-001", new Order("ORD-001", "张三", "MacBook Pro", "已发货", "2025-01-15"));
        ORDER_DB.put("ORD-002", new Order("ORD-002", "李四", "iPhone 16", "待发货", "2025-01-16"));
        ORDER_DB.put("ORD-003", new Order("ORD-003", "王五", "AirPods Pro", "已签收", "2025-01-10"));
    }

    /**
     * 查询订单信息 - returnDirect=true 直接返回结果给用户，不再经过模型
     */
    @Tool(description = "根据订单号查询订单详细信息，包括订单状态、商品和收货人", returnDirect = true)
    public String queryOrder(
            @ToolParam(description = "订单编号，格式为 ORD-XXX") String orderId,
            ToolContext toolContext) {
        String tenantId = (String) toolContext.getContext().get("tenantId");
        System.out.println("[ToolContext] tenantId = " + tenantId);

        Order order = ORDER_DB.get(orderId);
        if (order == null) {
            return "未找到订单：" + orderId;
        }
        return String.format("订单号: %s\n收货人: %s\n商品: %s\n状态: %s\n下单时间: %s",
                order.orderId(), order.customer(), order.product(), order.status(), order.date());
    }

    /**
     * 取消订单 - 正常流程，结果会经过模型
     */
    @Tool(description = "取消指定订单")
    public String cancelOrder(
            @ToolParam(description = "要取消的订单编号") String orderId,
            @ToolParam(description = "取消原因") String reason) {
        Order order = ORDER_DB.get(orderId);
        if (order == null) {
            return "未找到订单：" + orderId + "，无法取消";
        }
        if ("已签收".equals(order.status())) {
            return "订单 " + orderId + " 已签收，无法取消";
        }
        return "订单 " + orderId + " 已取消，原因：" + reason;
    }

    /**
     * 查询天气 - 演示多工具协调
     */
    @Tool(description = "查询指定城市的天气情况")
    public String queryWeather(@ToolParam(description = "城市名称") String city) {
        return city + "今天晴，气温22°C，适合出行。";
    }

    /**
     * 获取当前时间 - 演示基础 @Tool
     */
    @Tool(description = "获取当前日期和时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    record Order(String orderId, String customer, String product, String status, String date) {
    }
}
