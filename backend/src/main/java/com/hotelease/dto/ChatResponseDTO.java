package com.hotelease.dto;

import java.util.List;
import java.util.Map;

public class ChatResponseDTO {
    private String reply;
    private String intent;
    private List<Map<String, Object>> data;
    private boolean actionRequired;
    private String actionType;

    public ChatResponseDTO() {}

    public ChatResponseDTO(String reply, String intent) {
        this.reply = reply;
        this.intent = intent;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }

    public boolean isActionRequired() { return actionRequired; }
    public void setActionRequired(boolean actionRequired) { this.actionRequired = actionRequired; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
}
