package com.hotelease.controller;

import com.hotelease.dto.ChatRequestDTO;
import com.hotelease.dto.ChatResponseDTO;
import com.hotelease.service.ChatbotEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotEngineService chatbotEngineService;

    public ChatbotController(ChatbotEngineService chatbotEngineService) {
        this.chatbotEngineService = chatbotEngineService;
    }

    @PostMapping("/api/chat")
    public ResponseEntity<ChatResponseDTO> processChat(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = chatbotEngineService.processChatMessage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> processLegacyChat(@RequestBody Map<String, String> requestMap) {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage(requestMap.get("message"));
        request.setUserEmail(requestMap.get("email"));
        request.setRoomNumber(requestMap.get("room"));

        ChatResponseDTO response = chatbotEngineService.processChatMessage(request);

        Map<String, Object> map = new HashMap<>();
        map.put("ai", response.getReply());
        map.put("intent", response.getIntent());
        return ResponseEntity.ok(map);
    }
}
