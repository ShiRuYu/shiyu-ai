package com.shiyu.ai.runtimeconsole.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class ConsoleWebController {

    @GetMapping({"/console", "/console/"})
    public String index() {
        return "forward:/console/index.html";
    }
}
