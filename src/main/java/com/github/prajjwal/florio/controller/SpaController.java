package com.github.prajjwal.florio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
            "/",
            "/login",
            "/register",
            "/verify-otp",
            "/forgot-password",
            "/reset-password",
            "/dashboard",
            "/book",
            "/my-bookings",
            "/worker/dashboard",
            "/worker/jobs",
            "/admin",
            "/profile",
            "/oauth2/callback"
    })
    public String forward() {
        return "forward:/index.html";
    }
}