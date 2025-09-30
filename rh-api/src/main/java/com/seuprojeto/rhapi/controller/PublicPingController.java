package com.seuprojeto.rhapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicPingController {

    @GetMapping("/public/assinatura/ping")
    public String ping() {
        return "ok";
    }
}
