package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.gzf.sentinel.service.RTUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel
 * @description: 通过控制台对其进行限流
 * @author: Gaozf
 * @create: 2020-06-12 14:35
 **/
@RestController
public class DemoController{
    public static final String ROLE = "demo—resource";

    public DemoController() {
        initDemo();
    }

//    {
//        initDemo();
//    }

    private void initDemo() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(ROLE);
        rule1.setCount(2);
        // 基于QPS流控规则
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // 默认不区分调用来源
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    @GetMapping("/gzf1111")
    @SentinelResource(value = "demo—resource",blockHandler = "blockMehord")
    public String hello(){
        //initDemo();
//        System.out.println("hello world");
        return "hello world";
    }

    public String blockMehord(BlockException ex) {
        return "熔断";
    }

}
