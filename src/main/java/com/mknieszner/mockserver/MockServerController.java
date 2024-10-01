package com.mknieszner.mockserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class MockServerController {

    private final List<HarEntry> harEntries = new ArrayList<>();

    private static final String HAR_DIRECTORY = "src/main/resources/hars";

    @PostConstruct
    public void loadHarFiles() throws Exception {
        File folder = new File(HAR_DIRECTORY);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Katalog HAR nie istnieje: " + HAR_DIRECTORY);
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".har"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("Brak plików HAR w katalogu: " + HAR_DIRECTORY);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        for (File file : files) {
            System.out.println("Loading HAR file: " + file.getName());

            Map<String, Object> harData = objectMapper.readValue(file, Map.class);
            Map<String, Object> log = (Map<String, Object>) harData.get("log");
            List<Map<String, Object>> entries = (List<Map<String, Object>>) log.get("entries");

            for (Map<String, Object> entryMap : entries) {
                HarEntry harEntry = new HarEntry();
                HarRequest request = new HarRequest();
                HarResponse response = new HarResponse();

                Map<String, Object> requestMap = (Map<String, Object>) entryMap.get("request");
                request.setMethod((String) requestMap.get("method"));
                request.setUrl((String) requestMap.get("url"));

                List<Map<String, String>> headersList = (List<Map<String, String>>) requestMap.get("headers");
                request.setHeaders(convertHeadersListToMap(headersList));

                List<Map<String, String>> queryStringList = (List<Map<String, String>>) requestMap.get("queryString");
                request.setQueryString(convertHeadersListToMap(queryStringList));

                harEntry.setRequest(request);

                Map<String, Object> responseMap = (Map<String, Object>) entryMap.get("response");
                response.setStatus((int) responseMap.get("status"));

                Map<String, Object> content = (Map<String, Object>) responseMap.get("content");
                response.setContent((String) content.get("text"));

                List<Map<String, String>> responseHeadersList = (List<Map<String, String>>) responseMap.get("headers");
                response.setHeaders(convertHeadersListToMap(responseHeadersList));

                harEntry.setResponse(response);
                harEntries.add(harEntry);
            }
        }
    }

    private Map<String, String> convertHeadersListToMap(List<Map<String, String>> headersList) {
        return headersList.stream().collect(
                HashMap::new,
                (map, header) -> map.put(header.get("name"), header.get("value")),
                HashMap::putAll
        );
    }

    @GetMapping("/**")
    public ResponseEntity<String> handleGetRequest(HttpServletRequest request,
                                                   @RequestParam Map<String, String> allParams,
                                                   @RequestHeader Map<String, String> headers) {
        return handleRequest(request, allParams, "GET");
    }

    @PostMapping("/**")
    public ResponseEntity<String> handlePostRequest(HttpServletRequest request,
                                                    @RequestParam Map<String, String> allParams,
                                                    @RequestHeader Map<String, String> headers,
                                                    @RequestBody(required = false) String body) {
        return handleRequest(request, allParams, "POST");
    }

    @PutMapping("/**")
    public ResponseEntity<String> handlePutRequest(HttpServletRequest request,
                                                   @RequestParam Map<String, String> allParams,
                                                   @RequestHeader Map<String, String> headers,
                                                   @RequestBody(required = false) String body) {
        return handleRequest(request, allParams, "PUT");
    }

    @DeleteMapping("/**")
    public ResponseEntity<String> handleDeleteRequest(HttpServletRequest request,
                                                      @RequestParam Map<String, String> allParams,
                                                      @RequestHeader Map<String, String> headers) {
        return handleRequest(request, allParams, "DELETE");
    }

    private ResponseEntity<String> handleRequest(HttpServletRequest request,
                                                 Map<String, String> allParams,
                                                 String method) {
        String requestUri = request.getRequestURI();

        for (HarEntry entry : harEntries) {
            if (entry.getRequest().getMethod().equalsIgnoreCase(method)
                    && extractPathFromUrl(entry.getRequest().getUrl()).equalsIgnoreCase(requestUri)
                    && allParams.equals(entry.getRequest().getQueryString())) {

                HarResponse response = entry.getResponse();
                return ResponseEntity.status(response.getStatus())
                        .headers(httpHeaders -> {
                            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                                httpHeaders.set(header.getKey(), header.getValue());
                            }
                        })
                        .body(response.getContent());
            }
        }

        return ResponseEntity.status(404).body("No matching entry found in HAR files");
    }

    private String extractPathFromUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            return parsedUrl.getPath();
        } catch (Exception e) {
            throw new RuntimeException("Nie można sparsować URL: " + url, e);
        }
    }
}
