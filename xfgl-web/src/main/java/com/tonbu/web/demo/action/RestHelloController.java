package com.tonbu.web.demo.action;

import org.springframework.web.bind.annotation.*;

@RestController
public class RestHelloController {

    @RequestMapping(value = "/hello2", method = RequestMethod.GET)
    @ResponseBody
    public String hello(@RequestParam String name) {
        return "Hello " + name;
    }

}

