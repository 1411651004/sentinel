package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.gzf.sentinel.entry.User;
import com.gzf.sentinel.service.RTUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Slf4j
public class RTDemo {
    @Autowired
    private RTUserService userService;

    @GetMapping("/getUserByRTDegradeRule")
    public List<User> getUserByRTDegradeRule() throws Exception {

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

    /**
     * 注解中的fallback验证
     * TODO:失败了
     * @param id
     * @return
     */
    @GetMapping ("/sentinel-test/{id}")
    @SentinelResource( value = "SentinelTest",fallback = "SentinelTestException")
    public String SentinelTest(@PathVariable("id") long id){
        long defaultId = 10L;

        if (id < defaultId) {
            throw new RuntimeException ("id bad");
        }
        return "Sentinel test OKK!";
    }

    /**
     * fallback
     * @param id
     */
    public String SentinelTestException (long id,Throwable throwable) {

        log.error ("id={}",id);

        return "Sentinel test Error!";
    }


}
