package com.mknieszner.mockserver;

import lombok.Data;
import java.util.Map;

@Data
public class HarRequest {
    private String method;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> queryString;
}
