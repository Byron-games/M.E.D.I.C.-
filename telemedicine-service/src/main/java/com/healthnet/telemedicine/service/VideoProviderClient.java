package com.healthnet.telemedicine.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction over the video conferencing provider.
 *
 * Supports two modes:
 *   DAILY  – Daily.co REST API (requires API key, better for high-bandwidth)
 *   JITSI  – Self-hosted Jitsi Meet (free, works on low-bandwidth, good for rural areas)
 *
 * Configure via: healthnet.telemedicine.provider = DAILY | JITSI
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoProviderClient {

    @Value("${healthnet.telemedicine.provider:JITSI}")
    private String provider;

    @Value("${healthnet.telemedicine.daily.api-key:}")
    private String dailyApiKey;

    @Value("${healthnet.telemedicine.jitsi.base-url:https://meet.jit.si}")
    private String jitsiBaseUrl;

    @Value("${healthnet.telemedicine.base-url:https://meet.healthnet.com}")
    private String healthnetBaseUrl;

    private final RestTemplate restTemplate;

    public RoomDetails createRoom(String roomName, boolean lowBandwidth) {
        if ("DAILY".equalsIgnoreCase(provider) && !dailyApiKey.isBlank()) {
            return createDailyRoom(roomName);
        }
        // Default: use Jitsi (no API call needed, just construct URLs)
        return createJitsiRoom(roomName, lowBandwidth);
    }

    public void closeRoom(String roomId) {
        if ("DAILY".equalsIgnoreCase(provider) && !dailyApiKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(dailyApiKey);
                restTemplate.exchange(
                        "https://api.daily.co/v1/rooms/" + roomId,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        Void.class);
                log.info("Closed Daily.co room: {}", roomId);
            } catch (Exception e) {
                log.warn("Failed to close Daily.co room {}: {}", roomId, e.getMessage());
            }
        }
        // Jitsi rooms expire automatically; no explicit close needed
    }

    private RoomDetails createDailyRoom(String roomName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dailyApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("name", roomName);
        body.put("privacy", "private");

        Map<String, Object> properties = new HashMap<>();
        properties.put("max_participants", 2);
        properties.put("enable_recording", false);
        body.put("properties", properties);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.daily.co/v1/rooms",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class);

        String url = (String) response.getBody().get("url");
        return RoomDetails.builder()
                .roomId(roomName)
                .guestUrl(url + "?t=guest")
                .hostUrl(url + "?t=host")
                .build();
    }

    private RoomDetails createJitsiRoom(String roomName, boolean lowBandwidth) {
        String baseUrl = lowBandwidth ? jitsiBaseUrl : healthnetBaseUrl;
        String config = lowBandwidth ? "#config.resolution=360&config.constraints.video.height.ideal=360" : "";

        return RoomDetails.builder()
                .roomId(roomName)
                .guestUrl(baseUrl + "/" + roomName + config)
                .hostUrl(baseUrl + "/" + roomName + "?moderator=true" + config)
                .build();
    }

    @Data
    @lombok.Builder
    public static class RoomDetails {
        private String roomId;
        private String guestUrl;
        private String hostUrl;
    }
}
