package com.gzf.sentinel.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel
 * @description:
 * @author: Gaozf
 * @create: 2020-06-12 14:21
 **/
@Configuration
public class SentileConfig {

    @PostConstruct
    public void init(){
        //将自定义的阈值提示加载到应用中
        //      WebCallbackManager.setUrlBlockHandler(new DemoUrlBlockHandler());
        //黑白名单
        WebCallbackManager.setRequestOriginParser(new IpRequestOriginParser());
    }

}
