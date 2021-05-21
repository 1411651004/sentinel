package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.gzf.sentinel.service.RelationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel
 * @description: 关联模式下的流的验证
 * @author: Gaozf
 * @create: 2020-08-03 10:16
 **/
@RestController
public class RelationFlowController {

    @Resource
    RelationService relationService;

    {
        initRole();
    }

    private void initRole() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        FlowRule rule2 = new FlowRule();
        rule1.setResource("testA");
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setRefResource("testB");
        rule1.setCount(1);
        rule1.setLimitApp("default");

        rule2.setResource("testB");
        rule2.setCount(1);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setLimitApp("default");

        rules.add(rule1);
        rules.add(rule2);
        FlowRuleManager.loadRules(rules);
    }

    @GetMapping("/testA")
    @SentinelResource(value = "testA",blockHandler = "blockHandlerForGetUser")
    public String testA() {
        return "success testA!";
    }

    @GetMapping("/testB")
    @SentinelResource(value = "testB",blockHandler = "blockHandlerForGetUser")
    public String testB() {
        //return relationService.testB();

        return "success testB!";
    }

    public String blockHandlerForGetUser(BlockException ex) {
        return "熔断----block";
    }



}
