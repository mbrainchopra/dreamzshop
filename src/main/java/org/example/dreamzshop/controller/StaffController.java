package org.example.dreamzshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffController {

    @GetMapping("/staff/dashboard")
    public String dashboard() {
        return "staff/dashboard";
    }
}