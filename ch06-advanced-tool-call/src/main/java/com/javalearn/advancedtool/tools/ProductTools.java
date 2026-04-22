package com.javalearn.advancedtool.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * 商品工具类 - 演示多工具协调
 * <p>
 * 与 OrderTools 配合，模型会根据用户意图自动选择合适的工具
 */
public class ProductTools {

    @Tool(description = "搜索商品，根据关键词查找匹配的商品列表")
    public String searchProduct(@ToolParam(description = "搜索关键词") String keyword) {
        List<String> products = List.of(
                "MacBook Pro 14寸 - ￥14999",
                "MacBook Air 13寸 - ￥8999",
                "iPhone 16 Pro - ￥8999",
                "iPhone 16 - ￥6999",
                "AirPods Pro 2 - ￥1899",
                "iPad Pro 12.9 - ￥8999"
        );
        String result = products.stream()
                .filter(p -> p.toLowerCase().contains(keyword.toLowerCase()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("未找到相关商品");
        return "搜索结果：\n" + result;
    }

    @Tool(description = "获取商品价格")
    public String getProductPrice(@ToolParam(description = "商品名称") String productName) {
        return productName + " 的价格是 ￥8999";
    }
}
