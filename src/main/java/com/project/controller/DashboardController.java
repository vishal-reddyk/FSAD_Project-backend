package com.project.controller;

import org.springframework.web.bind.annotation.*;
import com.project.util.PasswordUtil;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {

        Map<String, Object> data = new HashMap<>();

        data.put("value", "₹85L");
        data.put("gain", "₹10.2L");
        data.put("improvements", "3/7");
        data.put("ideas", "12");

        return data;
    }
}