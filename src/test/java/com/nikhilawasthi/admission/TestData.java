package com.nikhilawasthi.admission;

import java.util.HashMap;
import java.util.Map;

/** Helpers that create test data. Every test uses its own unique email so tests never clash. */
final class TestData {

    private TestData() {
    }

    static String uniqueEmail() {
        return "student" + System.nanoTime() + "@example.com";
    }

    static Map<String, Object> validApplicant(String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", "Ravi Kumar");
        body.put("email", email);
        body.put("phone", "9123456780");
        body.put("course", "B.Tech");
        body.put("percentage", 82.5);
        return body;
    }
}
