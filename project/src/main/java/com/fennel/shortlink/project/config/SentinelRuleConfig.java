/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fennel.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        List<FlowRule> rules = new ArrayList<>();
//        FlowRule createOrderRule = new FlowRule();
//        createOrderRule.setResource("create_short-link");
//        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        createOrderRule.setCount(1);
//        rules.add(createOrderRule);
//        FlowRuleManager.loadRules(rules);
//    }

    // 为资源名定义静态常量，便于维护和复用
    private static final String RESOURCE_GENERATE = "generateShortLink";
    private static final String RESOURCE_REDIRECT = "redirectShortLink";

    @Override
    public void afterPropertiesSet() throws Exception {
        // 配置流控规则
        initFlowRules();
        // 配置熔断降级规则
        initDegradeRules();
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // =========================================================================
        // 规则1: 短链接生成接口 (QPS=800, 排队等待0.5s)
        // =========================================================================
        FlowRule generateRule = new FlowRule();
        generateRule.setResource(RESOURCE_GENERATE); // 资源名
        generateRule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 按 QPS 限流
        generateRule.setCount(800); // 阈值
        generateRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER); // 流控效果: 排队等待
        generateRule.setMaxQueueingTimeMs(500); // 最大排队超时时间: 500毫秒
        rules.add(generateRule);


        // =========================================================================
        // 规则2: 短链接跳转接口 (QPS=3000, 快速失败)
        // =========================================================================
        FlowRule redirectRule = new FlowRule();
        redirectRule.setResource(RESOURCE_REDIRECT); // 资源名
        redirectRule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 按 QPS 限流
        redirectRule.setCount(3000); // 阈值
        // 使用默认的流控效果 (CONTROL_BEHAVIOR_DEFAULT)，即快速失败
        rules.add(redirectRule);

        FlowRuleManager.loadRules(rules);
        System.out.println("Sentinel Flow Rules have been loaded successfully.");
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // =========================================================================
        // 规则3: 为短链接跳转接口配置熔断规则 (异常比例)
        // =========================================================================
        DegradeRule redirectDegradeRule = new DegradeRule();
        redirectDegradeRule.setResource(RESOURCE_REDIRECT); // 资源名，必须与流控规则中的资源名一致
        redirectDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO); // 按 异常比例 熔断
        redirectDegradeRule.setCount(0.2); // 比例阈值: 20%
        redirectDegradeRule.setMinRequestAmount(20); // 触发熔断的最小请求数
        redirectDegradeRule.setStatIntervalMs(1000); // 统计时长: 1000毫秒 (1秒)
        redirectDegradeRule.setTimeWindow(10); // 熔断时长: 10秒

        rules.add(redirectDegradeRule);

        // 加载所有熔断规则
        DegradeRuleManager.loadRules(rules);
        System.out.println("Sentinel Degrade Rules have been loaded successfully.");
    }
}
