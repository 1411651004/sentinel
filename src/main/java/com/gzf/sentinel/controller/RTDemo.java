package com.gzf.sentinel.controller;

import com.gzf.sentinel.entry.User;
import com.gzf.sentinel.service.RTUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel
 * @description: 熔断降级验证过程
 * @author: Gaozf
 * @create: 2020-06-12 16:44
 **/
@RestController
public class RTDemo {
    @Autowired
    private RTUserService userService;

    @GetMapping("/getUserByRTDegradeRule")
    public List<User> getUserByRTDegradeRule() throws InterruptedException {

        List<User> usersList = new ArrayList<User>();
        for (int i = 0; i < 50; i++) {
            usersList.add(userService.getUserByRTDegradeRule(i));
        }

        return usersList;
    }

    @GetMapping("/getUserByRATIODegradeRule")
    public List<User> getUserByRATIODegradeRule() throws InterruptedException {

        List<User> usersList = new ArrayList<User>();
        for (int i = 0; i < 50; i++) {
            usersList.add(userService.getUserByRATIODegradeRule(i));
        }

        return usersList;
    }


}
