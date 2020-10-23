package com.tonbu.web.demo.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/file")
@Controller
public class FileController {


    @RequestMapping("/")
    public String index() {

        return "demo/fileupload";

    }


}
