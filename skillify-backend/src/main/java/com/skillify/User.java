package com.skillify;

import jakarta.persistence.*;
import java.util.*;

@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(unique = true) public String email;
    public String name, passwordHash, token;
    public String college, degree, branch, studyYear, cgpa, targetRole, interests, skillLevel;

    /** Copies profile fields from a request body; missing fields keep their old value. */
    public void apply(Map<String, Object> b) {
        name = g(b, "name", name); college = g(b, "college", college); degree = g(b, "degree", degree);
        branch = g(b, "branch", branch); studyYear = g(b, "year", studyYear); cgpa = g(b, "cgpa", cgpa);
        targetRole = g(b, "targetRole", targetRole); interests = g(b, "interests", interests);
        skillLevel = g(b, "level", skillLevel);
    }
    static String g(Map<String, Object> b, String k, String d) { Object v = b.get(k); return v == null ? d : v.toString().trim(); }

    public Map<String, Object> profile() {
        var m = new LinkedHashMap<String, Object>();
        m.put("name", name); m.put("email", email); m.put("college", college); m.put("degree", degree);
        m.put("branch", branch); m.put("year", studyYear); m.put("cgpa", cgpa);
        m.put("targetRole", targetRole); m.put("interests", interests); m.put("level", skillLevel);
        return m;
    }
}
