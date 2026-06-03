package com.hong.aicodechat.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

@Component
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    public String getWeather(Map<String, Object> input, ToolContext ctx) {
        String city = (String) input.getOrDefault("city", "深圳");
        log.info("Weather tool called for city: {}", city);
        try {
            String encoded = java.net.URLEncoder.encode(city, StandardCharsets.UTF_8);
            URI uri = URI.create("https://wttr.in/" + encoded + "?format=j1&lang=zh");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();

            if (conn.getResponseCode() != 200) {
                return "无法获取 " + city + " 的天气信息，请检查城市名称是否正确。";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            reader.close();

            return formatWeather(city, body.toString());
        } catch (Exception e) {
            log.error("Weather query failed for {}", city, e);
            return "查询 " + city + " 天气时出错：" + e.getMessage();
        }
    }

    private String formatWeather(String city, String json) {
        try {
            int tempC = json.indexOf("\"temp_C\"");
            int feelsLike = json.indexOf("\"FeelsLikeC\"");
            int humidity = json.indexOf("\"humidity\"");
            int weatherDesc = json.indexOf("\"weatherDesc\"");
            int windKmph = json.indexOf("\"windspeedKmph\"");
            int cloud = json.indexOf("\"cloudcover\"");

            String temp = extractValue(json, tempC);
            String feel = extractValue(json, feelsLike);
            String hum = extractValue(json, humidity);
            String wDesc = extractDesc(json, weatherDesc);
            String wind = extractValue(json, windKmph);
            String cld = extractValue(json, cloud);

            return String.format(
                    "【%s 今日天气】\n温度: %s°C（体感 %s°C）\n天气: %s\n湿度: %s%%\n风速: %s km/h\n云量: %s%%",
                    city.toUpperCase(), temp, feel, wDesc, hum, wind, cld);
        } catch (Exception e) {
            return city + " 天气数据: " + json.substring(0, Math.min(500, json.length()));
        }
    }

    private String extractValue(String json, int start) {
        int col = json.indexOf(':', start);
        if (col < 0) return "N/A";
        int end = json.indexOf(',', col);
        if (end < 0) end = json.indexOf('}', col);
        if (end < 0) end = json.length();
        String raw = json.substring(col + 1, end).trim().replace("\"", "");
        return raw.isEmpty() ? "N/A" : raw;
    }

    private String extractDesc(String json, int start) {
        int arr = json.indexOf('[', start);
        if (arr < 0) return extractValue(json, start);
        int valStart = json.indexOf("\"value\"", arr);
        if (valStart < 0) return extractValue(json, start);
        return extractValue(json, valStart);
    }
}
