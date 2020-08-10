package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel
 * @description: sentinel加nacos的动态规则配置
 * @author: Gaozf
 * @create: 2020-07-13 17:11
 **/
@RestController
public class SentinelNacosController {

    //配置常量
    private static String RESOURCE_NAME = "hello";
    private static final String APP_NAME = "gzf";

    private static final String REMOTE_ADDRESS = "localhost";
    private static final String GROUP_ID = "DEFAULT_GROUP";
    private static final String FLOW_POSTFIX = "-sentinel-flow";

    public SentinelNacosController() {
        init();
    }

    private void init() {
//        List<FlowRule> rules = new ArrayList<FlowRule>();
//        FlowRule rule2 = new FlowRule();
//        rule2.setResource("hello");
//        rule2.setCount(1);
//        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule2.setLimitApp("default");
//        rules.add(rule2);
//        FlowRuleManager.loadRules(rules);

        // 使用 Nacos 数据源作为配置中心，需要在 REMOTE_ADDRESS 上启动一个 Nacos 的服务
        ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(REMOTE_ADDRESS, GROUP_ID,
                APP_NAME + FLOW_POSTFIX,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        // 为集群客户端注册动态规则源
        FlowRuleManager.register2Property(ds.getProperty());
    }

    @GetMapping("/hello")
    public String hello() {
        Entry entry = null;
        try {
            entry = SphU.entry(RESOURCE_NAME);
            return "hello sentinel";
        } catch (Exception e) {
            return "exception";
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
