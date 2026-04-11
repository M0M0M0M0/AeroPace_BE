package com.group1.shop_runner.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
//@CrossOrigin(origins = "*")

public class TestController {

    @GetMapping("/test1")
    public String test() {
        return "Hello, you are authenticated!";
    }
    @GetMapping("/test")
    public String admin(Authentication auth) {
//        System.out.println(auth.getAuthorities());
        return "Admin only";
    }

}