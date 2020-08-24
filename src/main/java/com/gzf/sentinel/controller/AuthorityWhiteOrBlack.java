package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * @program: sentinel
 * @description: 限流模式中的白名单是黑名单
 * @author: Gaozf
 * @create: 2020-08-12 16:10
 **/
@RestController
public class AuthorityWhiteOrBlack {

//    public AuthorityWhiteOrBlack() {
//        initRole();
//    }

    private void initRole() {
        AuthorityRule rule = new AuthorityRule();
        rule.setResource("whiteA");
        rule.setStrategy(RuleConstant.AUTHORITY_WHITE);
        rule.setLimitApp("127.0.0.2");
        AuthorityRuleManager.loadRules(Collections.singletonList(rule));
    }

    @GetMapping("/whiteA")
    @SentinelResource(value = "whiteA",blockHandler = "blockHandlerForGetUser")
    public String whiteA() {
        initRole();
        //ContextUtil.enter("whiteA","App3");
        return "success whiteA!";
    }

    @GetMapping("/whiteB")
    @SentinelResource(value = "whiteB",blockHandler = "blockHandlerForGetUser")
    public String whiteB() {
        return "success whiteB!";
    }

    @GetMapping("/balckA")
    @SentinelResource(value = "balckA",blockHandler = "blockHandlerForGetUser")
    public String balckA() {
        return "success balckA!";
    }

    @GetMapping("/balckB")
    @SentinelResource(value = "balckB",blockHandler = "blockHandlerForGetUser")
    public String balckB() {
        return "success balckB!";
    }

    public String blockHandlerForGetUser(BlockException ex) {
        return "熔断----block";
    }

}
