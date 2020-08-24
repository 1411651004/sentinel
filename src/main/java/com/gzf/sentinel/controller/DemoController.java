package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.gzf.sentinel.entity.ResourceRoleQps;
import com.gzf.sentinel.mapper.ResourceRoleQpsMapper;
import com.gzf.sentinel.config.DataSourceUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: sentinel
 * @description: 通过控制台对其进行限流
 * 修改规则数据源从数据库中拉取规则验证
 * @author: Gaozf
 * @create: 2020-06-12 14:35
 **/
@RestController
public class DemoController{

    @Resource
    ResourceRoleQpsMapper resourceRoleQpsMapper;

//    @Autowired
//    private JdbcTemplate jdbcTemplate;

    public static final String RESOURCE_NAME = "demo—resource";
    private static final String APP_NAME = "gzf";

    private static final String REMOTE_ADDRESS = "localhost";
    private static final String GROUP_ID = "DEFAULT_GROUP";
    private static final String FLOW_POSTFIX = "-sentinel-flow";

    public DemoController() {
        initDemo();
    }


    private void initDemo() {
//        List<FlowRule> rules = new ArrayList<FlowRule>();
//        FlowRule rule1 = new FlowRule();
//        rule1.setResource(RESOURCE_NAME);
//        rule1.setCount(2);
//        // 基于QPS流控规则
//        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        // 默认不区分调用来源
//        rule1.setLimitApp("default");
//        rules.add(rule1);
//        FlowRuleManager.loadRules(rules);
//        ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(REMOTE_ADDRESS, GROUP_ID,
//                APP_NAME + FLOW_POSTFIX,
//                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
//                }));
//        // 为集群客户端注册动态规则源
//        FlowRuleManager.register2Property(ds.getProperty());
    }

    @GetMapping("/sqlOrigin")
    @SentinelResource(value = "sqlOrigin",blockHandler = "blockMehord")
    public String sqlOrigin(){
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
        //initDemo();
        return "success";
    }

    @GetMapping("/gzf2222")
    public List<ResourceRoleQps> hello11(){

        return resourceRoleQpsMapper.selectAll();
    }

    public String blockMehord(BlockException ex) {
        return "熔断";
    }

    @GetMapping("/jdbc")
    public List<ResourceRoleQps> queryStudent() {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceUtils.getDataSource());
        return jdbcTemplate.query("select id, app_id, api, limit_qps, create_at from resource_role_qps"
                ,new RowMapper<ResourceRoleQps>(){
                    @Override
                    public ResourceRoleQps mapRow(ResultSet resultSet, int i) throws SQLException {
                        return new ResourceRoleQps(resultSet.getLong("id"),
                                resultSet.getString("app_id"),
                                resultSet.getString("api"),
                                resultSet.getLong("limit_qps"),
                                resultSet.getLong("create_at"));
                    }
                });

    }


}
