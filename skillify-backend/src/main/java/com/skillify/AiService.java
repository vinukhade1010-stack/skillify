package com.skillify;

import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.http.*;
import java.util.*;

/** Calls the Google Gemini API. The key lives only on this server. To use another AI, change only this class. */
@Service
public class AiService {
    @Value("${ai.key:}") String key;
    @Value("${ai.model}") String model;
    final HttpClient http = HttpClient.newHttpClient();
    final ObjectMapper om = new ObjectMapper();

    String call(String system, String prompt, boolean json) {
        if (key.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI key is not set on the server (GOOGLE_API_KEY)");
        try {
            Map<String, Object> cfg = new HashMap<>();
            cfg.put("maxOutputTokens", 8192);
            if (json) cfg.put("responseMimeType", "application/json");
            String body = om.writeValueAsString(Map.of(
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", cfg));
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                    .header("x-goog-api-key", key).header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            int c = res.statusCode();
            if (c == 400 || c == 403) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google rejected the request (" + c + "). Check GOOGLE_API_KEY and the model name.");
            if (c == 429) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Google AI limit reached. Wait a minute and try again.");
            if (c != 200) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI request failed (" + c + ")");
            StringBuilder sb = new StringBuilder();
            for (JsonNode p : om.readTree(res.body()).at("/candidates/0/content/parts")) sb.append(p.path("text").asText(""));
            if (sb.length() == 0) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an empty answer. Try again.");
            return sb.toString();
        } catch (ResponseStatusException e) { throw e;
        } catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI request failed. Try again."); }
    }

    public String ask(String system, String prompt) { return call(system, prompt, false); }

    public JsonNode askJson(String system, String prompt) {
        String t = call(system + " Reply with valid JSON only, no markdown, no extra text.", prompt, true).trim();
        try { return om.readTree(t.substring(t.indexOf('{'), t.lastIndexOf('}') + 1)); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an unreadable answer. Try again."); }
    }
}
