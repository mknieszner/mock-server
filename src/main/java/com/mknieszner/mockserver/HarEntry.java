package com.mknieszner.mockserver;

import lombok.Data;

@Data
public class HarEntry {
    private HarRequest request;
    private HarResponse response;
}
