package com.shiyu.ai.runtimeconsole.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 提供运行时控制台静态页面和资源的 HTTP 入口。 */
@Controller
public final class ConsoleWebController {

    @GetMapping({"/console", "/console/"})
    public String index() {
        return "forward:/console/index.html";
    }
}
