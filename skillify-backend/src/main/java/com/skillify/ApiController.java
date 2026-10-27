package com.skillify;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api")
public class ApiController {
    @Autowired UserRepo users;
    @Autowired ScoreRepo scores;
    @Autowired RoadmapRepo roadmaps;
    @Autowired AiService ai;
    @Autowired VideoService videos;
    final ObjectMapper om = new ObjectMapper();

    static User me(HttpServletRequest r) { return (User) r.getAttribute("user"); }
    static String ctx(User u) {
        return u.degree + " (" + u.branch + "), year " + u.studyYear + ", skill level " + u.skillLevel
                + ", target role " + u.targetRole + ", interests " + u.interests;
    }
    static String str(Map<String, Object> b, String k) {
        Object v = b.get(k); String s = v == null ? "" : v.toString().trim();
        if (s.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing field: " + k);
        return s;
    }

    /** Topics shown on the Home page, chosen by degree and branch. */
    @GetMapping("/topics")
    public Object topics(HttpServletRequest r) {
        User u = me(r);
        String k = (u.degree + " " + u.branch).toLowerCase();
        if (k.contains("mba") || k.contains("management")) return List.of("Marketing", "Finance", "Human Resources", "Operations", "Business Analytics");
        if (k.contains("bca") || k.contains("mca")) return List.of("Programming Basics", "Web Development", "DBMS", "Java", "Computer Networks");
        if (k.contains("cse") || k.contains("computer") || k.contains("information tech") || k.equals("b.tech it") || k.contains(" it"))
            return List.of("Java", "Data Structures", "DBMS", "Operating Systems", "Web Development", "Python");
        if (k.contains("sc")) return List.of("Mathematics", "Statistics", "Python", "Data Analysis");
        if (k.contains("mech")) return List.of("Thermodynamics", "Fluid Mechanics", "CAD", "Manufacturing");
        if (k.contains("ece") || k.contains("electronic") || k.contains("electrical"))
            return List.of("Digital Electronics", "Embedded Systems", "Signals and Systems", "Microcontrollers");
        return List.of("Communication Skills", "Aptitude", "Problem Solving", "MS Excel");
    }

    @PostMapping("/tutor/ask")
    public Object ask(HttpServletRequest r, @RequestBody Map<String, Object> b) {
        String sys = "You are Skilly, a friendly tutor for this student: " + ctx(me(r)) + ". Explain clearly in under 150 words with one example.";
        return Map.of("answer", ai.ask(sys, str(b, "question")));
    }

    @PostMapping("/quiz/generate")
    public Object generate(HttpServletRequest r, @RequestBody Map<String, Object> b) {
        String t = str(b, "topic");
        JsonNode j = ai.askJson("You write quiz questions for college students.",
                "Write 5 multiple-choice questions on \"" + t + "\" for this student: " + ctx(me(r))
                + ". Match the difficulty to their level. JSON shape: {\"questions\":[{\"q\":\"...\",\"o\":[\"A\",\"B\",\"C\",\"D\"],\"a\":0,\"e\":\"one-sentence explanation of why it is correct\"}]} "
                + "where a is the zero-based index of the correct option.");
        if (!j.path("questions").isArray() || j.path("questions").isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned no questions. Try again.");
        return Map.of("topic", t, "questions", j.get("questions"));
    }

    /** Saves the score, then asks the AI for a roadmap based on it. */
    @PostMapping("/quiz/submit")
    public Object submit(HttpServletRequest r, @RequestBody Map<String, Object> b) {
        User u = me(r);
        String t = str(b, "topic");
        int sc = b.get("score") instanceof Number n ? Math.max(0, Math.min(100, n.intValue())) : 0;
        List<?> wrong = b.get("wrong") instanceof List<?> l ? l : List.of();

        Score s = new Score(); s.userId = u.id; s.topic = t; s.score = sc; scores.save(s);

        Map<String, Object> out = new HashMap<>(Map.of("topic", t, "score", sc));
        try {
            JsonNode rm = ai.askJson("You are a career and study mentor.",
                "Student: " + ctx(u) + ". Topic: " + t + ". Quiz score: " + sc + "%. Questions answered wrong: " + wrong
                + ". Create a roadmap to master this skill. JSON shape: {\"summary\":\"\",\"weakAreas\":[\"\"],"
                + "\"stages\":[{\"title\":\"\",\"goal\":\"\",\"tasks\":[\"\"],\"weeks\":2,\"videoQuery\":\"YouTube search phrase for this stage\"}],"
                + "\"learnNext\":[{\"skill\":\"\",\"why\":\"\"}]}. Use 3 to 5 stages. learnNext lists 3 to 5 future skills for their target role.");
            RoadmapEntry e = roadmaps.findByUserIdAndTopic(u.id, t).orElseGet(RoadmapEntry::new);
            e.userId = u.id; e.topic = t; e.score = sc; e.body = rm.toString(); e.updatedAt = Instant.now();
            roadmaps.save(e);
            out.put("roadmap", rm);
        } catch (ResponseStatusException ex) {
            out.put("error", ex.getReason());   // score is saved even if the roadmap fails
        }
        return out;
    }

    @GetMapping("/roadmap")
    public Object roadmap(HttpServletRequest r) {
        List<Object> list = new ArrayList<>();
        for (RoadmapEntry e : roadmaps.findByUserId(me(r).id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("topic", e.topic); m.put("score", e.score); m.put("updatedAt", e.updatedAt.toString());
            try { m.put("roadmap", om.readTree(e.body)); } catch (Exception ex) { m.put("roadmap", null); }
            list.add(m);
        }
        return list;
    }

    /** Latest score per topic, e.g. {"Java":40,"DBMS":75} */
    @GetMapping("/progress")
    public Object progress(HttpServletRequest r) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (Score s : scores.findByUserIdOrderByCreatedAtAsc(me(r).id)) m.put(s.topic, s.score);
        return m;
    }

    @GetMapping("/videos")
    public Object videos(@RequestParam(defaultValue = "") String q) {
        return Map.of("configured", videos.configured(), "videos", videos.search(q));
    }

    @GetMapping("/profile")
    public Object profile(HttpServletRequest r) {
        User u = me(r);
        Map<String, Object> m = u.profile();
        List<Object> hist = new ArrayList<>();
        for (Score s : scores.findByUserIdOrderByCreatedAtAsc(u.id))
            hist.add(Map.of("topic", s.topic, "score", s.score, "date", s.createdAt.toString()));
        m.put("history", hist);
        return m;
    }

    @PutMapping("/profile")
    public Object updateProfile(HttpServletRequest r, @RequestBody Map<String, Object> b) {
        User u = me(r);
        u.apply(b);
        users.save(u);
        return u.profile();
    }
}
