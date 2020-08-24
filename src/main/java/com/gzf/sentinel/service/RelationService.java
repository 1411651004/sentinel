package com.gzf.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.gzf.sentinel.mapper.ResourceRoleQpsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel
 * @description: 关联模式
 * @author: Gaozf
 * @create: 2020-08-03 17:04
 **/
@Service
public class RelationService {

    @Autowired
    ResourceRoleQpsMapper resourceRoleQpsMapper;
    {
        initRole();
    }

    private void initRole() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        FlowRule rule2 = new FlowRule();
//        rule1.setResource("testA");
//        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule1.setRefResource("testB");
//        rule1.setCount(1);
//        rule1.setLimitApp("default");

        rule2.setResource("ceshiB");
        rule2.setCount(1);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setLimitApp("default");

        //rules.add(rule1);
        rules.add(rule2);
        FlowRuleManager.loadRules(rules);
    }

    @SentinelResource(value = "ceshiB",blockHandler = "blockHandlerForGetUser")
    public String testB() {
        //TODO：规则必须带进来
        System.out.println(resourceRoleQpsMapper.selectAll());
        initRole();
        return "success testB!";
    }

    public String blockHandlerForGetUser(BlockException ex) {
        return "熔断----block";
    }
}
