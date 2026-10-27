package com.skillify;

import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Finds REAL embeddable videos with the YouTube Data API (the AI never invents video links). */
@Service
public class VideoService {
    @Value("${youtube.key:}") String key;
    final HttpClient http = HttpClient.newHttpClient();
    final ObjectMapper om = new ObjectMapper();

    public boolean configured() { return !key.isBlank(); }

    public List<Map<String, String>> search(String q) {
        if (!configured() || q.isBlank()) return List.of();
        try {
            String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&videoEmbeddable=true&maxResults=3&q="
                    + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&key=" + key;
            HttpResponse<String> res = http.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YouTube request failed (" + res.statusCode() + ")");
            List<Map<String, String>> out = new ArrayList<>();
            for (JsonNode i : om.readTree(res.body()).path("items"))
                out.add(Map.of("videoId", i.at("/id/videoId").asText(), "title", i.at("/snippet/title").asText(),
                        "channel", i.at("/snippet/channelTitle").asText()));
            return out;
        } catch (ResponseStatusException e) { throw e;
        } catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not load videos. Try again."); }
    }
}
