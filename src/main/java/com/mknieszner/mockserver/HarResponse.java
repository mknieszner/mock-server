package com.mknieszner.mockserver;

import lombok.Data;
import java.util.Map;

@Data
public class HarResponse {
    private int status;
    private String content;
    private Map<String, String> headers;
}
