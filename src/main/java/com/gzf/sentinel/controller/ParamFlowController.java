package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @program: sentinel-tutorial
 * @description: 集群热点参数限流规则
 * @author: Gaozf
 * @create: 2020-07-24 16:58
 **/
@RestController
public class ParamFlowController {
    private static String RESOURCE_NAME = "freqParam";

    private static final String APP_NAME = "single";

    private static final String REMOTE_ADDRESS = "localhost";
    private static final String GROUP_ID = "DEFAULT_GROUP";

    private static final String PARAM_FLOW_POSTFIX = "-param-rules";

    public ParamFlowController(){
        //registerClusterFlowRuleProperty();
        init();
    }

    private void init() {
        // 定义热点限流的规则，对第一个参数设置 qps 限流模式，阈值为5
        ParamFlowRule rule = new ParamFlowRule(RESOURCE_NAME)
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(5);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }
    /**
     * 注册动态规则Property
     * 当client与Server连接中断，退化为本地限流时需要用到的该规则
     */
    private void registerClusterFlowRuleProperty() {
        // 使用 Nacos 数据源作为配置中心，需要在 REMOTE_ADDRESS 上启动一个 Nacos 的服务
        ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(REMOTE_ADDRESS, GROUP_ID,
                APP_NAME + PARAM_FLOW_POSTFIX,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        // 为集群客户端注册动态规则源
        FlowRuleManager.register2Property(ds.getProperty());
    }

    /**
     * 热点参数限流
     * 构造不同的uid的值，并且以不同的频率来请求该方法，查看效果
     */
    @GetMapping("/freqParamFlow")
    public String freqParamFlow(@RequestParam("uid") Long uid, @RequestParam("ip") Long ip) {
        Entry entry = null;
        String retVal;
        try{
            // 只对参数 uid 的值进行限流，参数 ip 的值不进行限制
            entry = SphU.entry(RESOURCE_NAME, EntryType.IN,1,uid);
            retVal = "passed";
        }catch(BlockException e){
            retVal = "blocked";
        }finally {
            if(entry!=null){
                entry.exit();
            }
        }
        return retVal;
    }

    @GetMapping("/freqParamFlowTwo")
    public String freqParamFlowTwo(@RequestParam("uid") Long uid, @RequestParam("ip") Long ip) {
        Entry entry = null;
        String retVal;
        try{
            // 只对参数 uid 的值进行限流，参数 ip 的值不进行限制
            entry = SphU.entry(RESOURCE_NAME, EntryType.IN,1);
            retVal = "passed";
        }catch(BlockException e){
            retVal = "blocked";
        }finally {
            if(entry!=null){
                entry.exit();
            }
        }
        return retVal;
    }
}
