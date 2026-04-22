package com.javalearn.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

/**
 * 天气工具 - 通过 MCP 暴露给外部 Client
 * <p>
 * 使用 @Tool 注解定义工具，Spring AI MCP Server Boot Starter
 * 会自动扫描并注册为 MCP 工具。
 */
@Service
public class WeatherTools {

    private static final Map<String, String[]> WEATHER_DATA = Map.of(
            "北京", new String[]{"晴", "多云", "小雨"},
            "上海", new String[]{"多云", "阴", "晴"},
            "深圳", new String[]{"晴", "雷阵雨", "多云"},
            "广州", new String[]{"多云", "晴", "大雨"}
    );

    @Tool(description = "查询指定城市的当前天气情况，包括温度、湿度和天气状况")
    public String getWeather(@ToolParam(description = "城市名称，如：北京、上海、深圳") String city) {
        String[] conditions = WEATHER_DATA.getOrDefault(city, new String[]{"晴", "多云"});
        String condition = conditions[new Random().nextInt(conditions.length)];
        int temp = 15 + new Random().nextInt(20);
        int humidity = 40 + new Random().nextInt(40);

        return String.format("城市: %s\n天气: %s\n温度: %d°C\n湿度: %d%%", city, condition, temp, humidity);
    }

    @Tool(description = "查询指定城市未来几天的天气预报")
    public String getForecast(
            @ToolParam(description = "城市名称") String city,
            @ToolParam(description = "预报天数，1-7天") int days) {
        if (days < 1) days = 1;
        if (days > 7) days = 7;

        StringBuilder sb = new StringBuilder(city + " 未来" + days + "天天气预报：\n");
        String[] conditions = {"晴", "多云", "小雨", "阴", "大雨"};
        Random random = new Random();
        for (int i = 1; i <= days; i++) {
            int temp = 15 + random.nextInt(20);
            String cond = conditions[random.nextInt(conditions.length)];
            sb.append(String.format("  第%d天: %s, %d°C\n", i, cond, temp));
        }
        return sb.toString();
    }
}
