package com.gzf.sentinel.controller;

import com.gzf.sentinel.service.RTUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: sentinel
 * @description: 通过控制台对其进行限流
 * @author: Gaozf
 * @create: 2020-06-12 14:35
 **/
@RestController
public class DemoController {

    @Autowired
    private RTUserService userService;

    @RequestMapping("/gzf")
    public String hello(){
//        System.out.println("hello world");
        return "hello world";
    }

//    @RequestMapping("/demo")
//    public String test(){
////        System.out.println("hello world");
//        userService.doSomeThing("demo");
//        return "hello world";
//    }
}
