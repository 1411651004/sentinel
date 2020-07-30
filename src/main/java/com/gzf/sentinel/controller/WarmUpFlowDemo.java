package com.gzf.sentinel.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: sentinel
 * @description: 冷启动验证方案
 * @author: Gaozf
 * @create: 2020-06-16 10:36
 **/
@RestController
public class WarmUpFlowDemo {

    private static final String KEY = "gzf";
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;

    private static final int threadCount = 100;

    private static int seconds = 60 + 40;

    @RequestMapping("/gzfDemo")
    public void hello() throws InterruptedException {
        //        System.out.println("hello world");
        //return "hello world";
        initFlowRule();
        // trigger Sentinel internal init
        Entry entry = null;
        try {
            entry = SphU.entry(KEY);
        } catch (Exception e) {

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
        // first make the system run on a very low condition
        // 创建3个WarmUpTask线程, 模拟一个系统处于一个低水平流量
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new WarmUpTask());
            t.setName("sentinel-warmup-task");
            t.start();
        }
        /**
         *
         * WarmUpTask线程运行20s后,再同时启动100个线程,
         * 设置其休眠时间小于50ms, 这样就模拟造成了访问资源的流量突增,
         * 一是可以查看后台console观察流量变化数值, 而是查看监控台的实时监控,
         * 能比较直观的看见warm up过程.
         */
        // 20s开始有突增的流量进来, 访问资源
        Thread.sleep(20000);

        // 创建一个100线程, 模拟突增的流量访问被保护的资源
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask());
            t.setName("sentinel-run-task");
            t.start();
        }
    }

    public static void main(String[] args) throws Exception {
        initFlowRule();
        // trigger Sentinel internal init
        Entry entry = null;
        try {
            entry = SphU.entry(KEY);
        } catch (Exception e) {

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
        // first make the system run on a very low condition
        // 创建3个WarmUpTask线程, 模拟一个系统处于一个低水平流量
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new WarmUpTask());
            t.setName("sentinel-warmup-task");
            t.start();
        }
        /**
         *
         * WarmUpTask线程运行20s后,再同时启动100个线程,
         * 设置其休眠时间小于50ms, 这样就模拟造成了访问资源的流量突增,
         * 一是可以查看后台console观察流量变化数值, 而是查看监控台的实时监控,
         * 能比较直观的看见warm up过程.
         */
        // 20s开始有突增的流量进来, 访问资源
        Thread.sleep(20000);

        // 创建一个100线程, 模拟突增的流量访问被保护的资源
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask());
            t.setName("sentinel-run-task");
            t.start();
        }
    }

    private static void initFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        // 设置最大阈值为20
        // rule1.setCount(20);
        // 这里设置QPS最大的阈值1000, 便于查看变化曲线
        rule1.setCount(1000);
        // 基于QPS流控规则
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // 默认不区分调用来源
        rule1.setLimitApp("default");
        // 流控效果, 采用warm up冷启动方式
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        // 在一定时间内逐渐增加到阈值上限，给冷系统一个预热的时间，避免冷系统被压垮。
        // warmUpPeriodSec 代表期待系统进入稳定状态的时间（即预热时长）。
        // 这里预热时间为1min, 便于在dashboard控制台实时监控查看QPS的pass和block变化曲线
        rule1.setWarmUpPeriodSec(60); // 默认值为10s
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    //WarmUpTask线程休眠小于2s, 通过控制休眠时间, 达到控制访问资源的流量处于一个较低的水平.
    static class WarmUpTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    // token acquired, means pass
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }
                Random random2 = new Random();
                try {
                    // 随机休眠时间<2s, 通过设置休眠时间(挡板业务耗时), 模拟访问资源的流量大小
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(2000));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

        static class RunTask implements Runnable {
            @Override
            public void run() {
                while (!stop) {
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                        pass.addAndGet(1);
                    } catch (BlockException e1) {
                        //以原子方式将当前值加 1。
                        block.incrementAndGet();
                    } catch (Exception e2) {
                        // biz exception
                    } finally {
                        total.incrementAndGet();
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                    Random random2 = new Random();
                    try {
                        // 随机休眠时间<50ms, 通过设置休眠时间, 模拟访问资源的流量大小
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }

    //创建并启动一个TimerTask线程, 统计每一秒的pass, block, total这三个指标
    static class TimerTask implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("begin to statistic!!!");
            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;
                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;
                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;
                System.out.println("currentTimeMillis:" + TimeUtil.currentTimeMillis() + ", totalSeconds:"
                        + TimeUtil.currentTimeMillis() / 1000 + ", currentSecond:"
                        + (TimeUtil.currentTimeMillis() / 1000) % 60 + ", total:" + oneSecondTotal
                        + ", pass:" + oneSecondPass + ", block:" + oneSecondBlock);
                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get() + ", block:" + block.get());
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}
