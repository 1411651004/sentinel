package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.gzf.sentinel.entity.ResourceRoleQps;
import com.gzf.sentinel.config.DataSourceUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: sentinel
 * @description: sentinel集成sql规则数据源
 * @author: Gaozf
 * @create: 2020-08-11 10:38
 **/
public class MysqlRefreshableDataSource extends AutoRefreshDataSource<List<ResourceRoleQps>,List<FlowRule>> {

    private static final long DEFAULT_REFRESH_MS = 3000;

    private static final Converter<List<ResourceRoleQps>, List<FlowRule>> flowRuleListParser =
            source -> source.stream().map(x -> new FlowRule()).collect(Collectors.toList());

    public MysqlRefreshableDataSource(Converter<List<ResourceRoleQps>, List<FlowRule>> configParser) {
        super(configParser, DEFAULT_REFRESH_MS);
        firstLoad();
    }

    public MysqlRefreshableDataSource() throws Exception {
        super(flowRuleListParser,DEFAULT_REFRESH_MS);
        this.close();
    }

    private void firstLoad() {
        try {
            List<FlowRule> newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Throwable e) {
            System.out.println(e);
            RecordLog.info("loadConfig exception", e);
        }
    }

    @Override
    public List<ResourceRoleQps> readSource() throws Exception {

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

        //List<ResourceRoleQps> resourceRoleQps = resourceRoleQpsMapper.selectAll();
//        List<ResourceRoleQps> resourceRoleQps = new ArrayList<>();
//
//        ResourceRoleQps resource = new ResourceRoleQps();
//        resource.setAppId("default");
//        resource.setId(1L);
//        resource.setCreateAt(1597129380812L);
//        resource.setLimitQps(1L);
//        resource.setApi("gzf1111");
//        resourceRoleQps.add(resource);
//        System.out.println(resourceRoleQps.toString());
//        System.out.println("##########执行完毕##########");
//        return resourceRoleQps;
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
