package tech.yakov.AtemProxy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.yakov.AtemProxy.service.EchoClientService;

@RestController
public class ApiRestController {
    private EchoClientService echoClientService;

    public ApiRestController(EchoClientService echoClientService) {
        this.echoClientService = echoClientService;
    }

    @GetMapping("/test")
    public String getTest() {
        return "name";
    }

}
