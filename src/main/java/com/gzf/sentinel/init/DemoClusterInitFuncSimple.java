/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gzf.sentinel.init;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * @author gzf
 */
public class DemoClusterInitFuncSimple implements InitFunc {

    private static final String APP_NAME = AppNameUtil.getAppName();

    private final String remoteAddress = "localhost";
    // 这里需要注意的是，我们使用Nacos指定namespace后，这里获取的实际不是namespace的名称clusterDemo，
    // 而是其对应的一串ID，大家可以从各自的Nacos上找到
    private final String nacosNamespace = "11f4068f-d5e7-4c97-af73-b9b1b037f5bd";// clusterDemo
    private final String groupId = "DEFAULT_GROUP";
    private Properties properties = new Properties();

    private final String flowDataId = APP_NAME + DemoConstants.FLOW_POSTFIX;
    private final String paramDataId = APP_NAME + DemoConstants.PARAM_FLOW_POSTFIX;
    private final String configDataId = APP_NAME + "-cluster-client-config";
    private final String clusterMapDataId = APP_NAME + DemoConstants.CLUSTER_MAP_POSTFIX;

    @Override
    public void init() throws Exception {
        // 使用namespace的方式加载Nacos配置
        properties.put(PropertyKeyConst.SERVER_ADDR, remoteAddress);
        properties.put(PropertyKeyConst.NAMESPACE, nacosNamespace);


        // client：加载FlowRule（降级规则）
        initDynamicRuleProperty();

        // client：加载ClusterClientConfig（requestTimeout）
        initClientConfigProperty();
        // client：加载ClusterClientAssignConfig（serverHost、serverPort）
        initClientServerAssignProperty();

        // server：加载集群规则，namespace下对应的FlowRule
        registerClusterRuleSupplier();
        // server：从assignMap中获取ServerTransportConfig（port、idleSeconds）
        initServerTransportConfigProperty();

        // 根据我们的clusterDemo-cluster-map配置，设置当前应用状态（CLIENT/SERVER/NOT_STARTED）
        initStateProperty();
    }

    // 这个最简单，本地加载降级规则，没啥好说的
    private void initDynamicRuleProperty() {
        ReadableDataSource<String, List<FlowRule>> ruleSource = new NacosDataSource<>(properties, groupId,
                flowDataId, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(ruleSource.getProperty());
    }

    // client端加载requestTimeout配置
    private void initClientConfigProperty() {
        ReadableDataSource<String, ClusterClientConfig> clientConfigDs = new NacosDataSource<>(properties, groupId,
                configDataId, source -> JSON.parseObject(source, new TypeReference<ClusterClientConfig>() {}));
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDs.getProperty());
    }

    // server端加载port
    private void initServerTransportConfigProperty() {
        ReadableDataSource<String, ServerTransportConfig> serverTransportDs = new NacosDataSource<>(properties, groupId,
                clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                    // 主要在这里，通过clusterDemo-cluster-map配置的值中的machineID来比对当前应用IP:port是否符合，符合则代表是server端
                    // 获取配置中的port值
                    .flatMap(this::extractServerTransportConfig)
                    .orElse(null);
        });
        ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getProperty());
    }

    // 这个是最关键的，根据namespace来动态从Nacos中获取FlowRule
    // namespace可以主动加载，通过代码ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(APP_NAME));
    // 也可以不写，在启动项中添加project.name=xxx，则namespace默认取该配置项值
    private void registerClusterRuleSupplier() {
        // Register cluster flow rule property supplier which creates data source by namespace.
        // Flow rule dataId format: ${namespace}-flow-rules
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(properties, groupId,
                    namespace + DemoConstants.FLOW_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            return ds.getProperty();
        });
    }

    // 这里主要是通过map配置项中的clientSet，比对当前应用的ip:port来确认当前是否client端，如果是，则设置serverIp:serverPort为配置中的ip:port
    private void initClientServerAssignProperty() {
        // Cluster map format:
        // [{"clientSet":["169.254.207.96@8729","169.254.207.96@8727"],"ip":"169.254.207.96","machineId":"169.254.207.96@8720","port":7717}]
        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
        ReadableDataSource<String, ClusterClientAssignConfig> clientAssignDs = new NacosDataSource<>(properties, groupId,
                clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                    // 主要在这里
                    .flatMap(this::extractClientAssignment)
                    .orElse(null);
        });
        ClusterClientConfigManager.registerServerAssignProperty(clientAssignDs.getProperty());
    }

    // 这里同样很关键，通过map配置中提前设定好的clientSet，machineID来确定当前应用是server还是client
    private void initStateProperty() {
        // Cluster map format:
        // [{"clientSet":["169.254.207.96@8729","169.254.207.96@8727"],"ip":"169.254.207.96","machineId":"169.254.207.96@8720","port":7717}]
        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
        ReadableDataSource<String, Integer> clusterModeDs = new NacosDataSource<>(properties, groupId,
                clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                    // 主要在这里
                    .map(this::extractMode)
                    .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
        });
        ClusterStateManager.registerProperty(clusterModeDs.getProperty());
    }

    private int extractMode(List<ClusterGroupEntity> groupList) {
        // If any server group machineId matches current, then it's token server.
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return ClusterStateManager.CLUSTER_SERVER;
        }
        // If current machine belongs to any of the token server group, then it's token client.
        // Otherwise it's unassigned, should be set to NOT_STARTED.
        boolean canBeClient = groupList.stream()
                .flatMap(e -> e.getClientSet().stream())
                .filter(Objects::nonNull)
                .anyMatch(e -> e.equals(getCurrentMachineId()));
        return canBeClient ? ClusterStateManager.CLUSTER_CLIENT : ClusterStateManager.CLUSTER_NOT_STARTED;
    }

    private Optional<ServerTransportConfig> extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
        return groupList.stream()
                .filter(this::machineEqual)
                .findAny()
                .map(e -> new ServerTransportConfig().setPort(e.getPort()).setIdleSeconds(600));
    }

    private Optional<ClusterClientAssignConfig> extractClientAssignment(List<ClusterGroupEntity> groupList) {
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return Optional.empty();
        }
        // Build client assign config from the client set of target server group.
        for (ClusterGroupEntity group : groupList) {
            if (group.getClientSet().contains(getCurrentMachineId())) {
                String ip = group.getIp();
                Integer port = group.getPort();
                return Optional.of(new ClusterClientAssignConfig(ip, port));
            }
        }
        return Optional.empty();
    }

    private boolean machineEqual(/*@Valid*/ ClusterGroupEntity group) {
        return getCurrentMachineId().equals(group.getMachineId());
    }

    private String getCurrentMachineId() {
        // Note: this may not work well for container-based env.
//        return HostNameUtil.getIp() + SEPARATOR + TransportConfig.getRuntimePort();

        return HostNameUtil.getIp() + SEPARATOR + TransportConfig.getPort();
    }

    private static final String SEPARATOR = "@";

}