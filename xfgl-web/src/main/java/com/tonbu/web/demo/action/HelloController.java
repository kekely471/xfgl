package com.tonbu.web.demo.action;

import com.tonbu.web.demo.service.TestService;
import com.tonbu.framework.exception.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloController {


    @Autowired
    TestService testService;

    @RequestMapping("/")
    public String index(Model map) {
        // 加入一个属性，用来在模板中读取
        map.addAttribute("host", "http://blog.didispace.com");
        // return模板文件的名称，对应src/main/resources/templates/index.html

        return "index";

    }

    @RequestMapping("/hello3")
    public String hello3() {
        return "hello3";
    }

    @RequestMapping("/hello")
    public String indexHello() throws Exception {
        throw new Exception("发生错误");
    }
    @RequestMapping("/json")
    public String json() throws JSONException {
        throw new JSONException("发生严重错误：10001");
    }
}

