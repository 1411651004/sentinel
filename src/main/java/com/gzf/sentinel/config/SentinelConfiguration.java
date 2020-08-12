package com.gzf.sentinel.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.gzf.sentinel.controller.MysqlRefreshableDataSource;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

/**
 * @program: sentinel
 * @description:
 * @author: Gaozf
 * @create: 2020-08-11 11:51
 **/
//@Configuration
public class SentinelConfiguration {


//    @Bean
//    public Converter converter() {
//        return (Converter<List<ResourceRoleQps>, List<FlowRule>>) source ->
//                source.stream().map(openApiAppIdApiQps -> {
//                    FlowRule flowRule = new FlowRule();
//                    flowRule.setResource(openApiAppIdApiQps.getApi());
//                    flowRule.setCount(openApiAppIdApiQps.getLimitQps());
//                    flowRule.setLimitApp(openApiAppIdApiQps.getAppId());
//                    flowRule.setGrade(1);
//                    flowRule.setStrategy(0);
//                    flowRule.setControlBehavior(0);
//                    return flowRule;
//                }).collect(Collectors.toList());
//    }

    @PostConstruct
    public void doInit() {
        // 自定义限流参数
        WebCallbackManager.setRequestOriginParser(request -> {
            String origin = request.getParameter("appid");
            return origin != null ? origin : "";
        });
        // 自定义限流返回的响应
//        WebCallbackManager.setUrlBlockHandler((request, response, ex) -> {
//            Message message = Message.error(IExceptionCode.OPENAPI_TOO_MANY_REQ_EXCEPTION, "请求过于频繁");
//            response.setContentType("application/json;charset=UTF-8");
//            Gson gson = new GsonBuilder()
//                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
//                    .create();
//            response.getOutputStream().write(gson.toJson(message).getBytes());
//        });

        ReadableDataSource readableDataSource = new MysqlRefreshableDataSource(source ->
                source.stream().map(openApiAppIdApiQps -> {
                    FlowRule flowRule = new FlowRule();
                    flowRule.setResource(openApiAppIdApiQps.getApi());
                    flowRule.setCount(openApiAppIdApiQps.getLimitQps());
                    flowRule.setLimitApp(openApiAppIdApiQps.getAppId());
                    flowRule.setGrade(1);
                    flowRule.setStrategy(0);
                    flowRule.setControlBehavior(0);
                    return flowRule;
                }).collect(Collectors.toList())
        );
        // 自定义拉取数据源
        FlowRuleManager.register2Property(readableDataSource.getProperty());
    }
}
