package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: sentinel
 * @description: 削峰填谷（匀速请求）
 * @author: Gaozf
 * @create: 2020-06-19 14:41
 **/
@RestController
public class PaceFlowController {

    private static final String            KEY        = "abc";

    private static volatile CountDownLatch countDown;

    // 模拟产生100个请求
    private static final Integer           requestQps = 1000;
    private static final Integer           count      = 100;
    private static final AtomicInteger done       = new AtomicInteger();
    private static final AtomicInteger     pass       = new AtomicInteger();
    private static final AtomicInteger     block      = new AtomicInteger();

    @GetMapping("/paceFlow")
    public void paceFlow() throws InterruptedException{
        System.out.println("pace behavior");
        countDown = new CountDownLatch(1);
        // queuing队列方式, 匀速处理流量
        initPaceFlowRule();
        // 直接并发同时启动1000个线程, 模拟1000个并发请求资源
        simulatePulseFlow();
        countDown.await();

        System.out.println("done");
        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());

        System.out.println();
    }

    @GetMapping("/defaltFlow")
    public void defaltFlow() throws InterruptedException{
        System.out.println("default behavior");
        TimeUnit.SECONDS.sleep(2);
        countDown = new CountDownLatch(1);
        // 默认方式, 立即拒绝多余流量
        initDefaultFlowRule();
        // 并发同时启动1000个线程, 模拟1000个并发请求资源
        simulatePulseFlow();
        countDown.await();
        System.out.println("done");
        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());
        System.exit(0);
    }

//    public static void main(String[] args) throws InterruptedException {
//        System.out.println("pace behavior");
//        countDown = new CountDownLatch(1);
//        // queuing队列方式, 匀速处理流量
//        initPaceFlowRule();
//        // 直接并发同时启动100个线程, 模拟100个并发请求资源
//        simulatePulseFlow();
//        countDown.await();
//
//        System.out.println("done");
//        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());
//
//        System.out.println();
//
//        System.out.println("default behavior");
//        TimeUnit.SECONDS.sleep(2);
//        // 重新计数开始
//        done.set(0);
//        pass.set(0);
//        block.set(0);
//        countDown = new CountDownLatch(1);
//        // 默认方式, 立即拒绝多余流量
//        initDefaultFlowRule();
//        // 并发同时启动100个线程, 模拟100个并发请求资源
//        simulatePulseFlow();
//        countDown.await();
//        System.out.println("done");
//        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());
//        System.exit(0);
//
//    }

    private static void initPaceFlowRule() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(count);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        /*
         * CONTROL_BEHAVIOR_RATE_LIMITER means requests more than threshold will be queueing in the queue, until the
         * queueing time is more than {@link FlowRule#maxQueueingTimeMs}, the requests will be rejected.
         */
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);

        // 这里设置的等待处理时间较大, 让系统能平稳的处理所有的请求
        rule1.setMaxQueueingTimeMs(20 * 1000);// 表示每一个请求的最长等待时间20s

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void initDefaultFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(count);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        // CONTROL_BEHAVIOR_DEFAULT means requests more than threshold will be rejected immediately.
        // CONTROL_BEHAVIOR_DEFAULT将超过阈值的流量立即拒绝掉.
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void simulatePulseFlow() {
        for (int i = 0; i < requestQps; i++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    long startTime = TimeUtil.currentTimeMillis();
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                        pass.incrementAndGet();
                    } catch (BlockException e1) {
                        System.out.println("===>BlockException");
                        block.incrementAndGet();
                    } catch (Exception e2) {
                        // biz exception
                    } finally {
                        if (entry != null) {
                            entry.exit();
                            // pass.incrementAndGet();
                            long cost = TimeUtil.currentTimeMillis() - startTime;
                            System.out.println(TimeUtil.currentTimeMillis() + " one request pass, cost " + cost
                                    + " ms");
                        }
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(5);
                    } catch (InterruptedException e1) {
                        // ignore
                    }

                    if (done.incrementAndGet() >= requestQps) {
                        countDown.countDown();
                    }
                }
            }, "Thread " + i);
            thread.start();
        }
    }
}
