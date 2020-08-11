package com.gzf.sentinel.service;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.gzf.sentinel.entry.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @program: sentinel
 * @description:
 * @author: Gaozf
 * @create: 2020-06-12 16:40
 **/
@Service
@Slf4j
public class RTUserService {
    public static final String USER_DEGRADERULE_RES = "userDegradeRuleResource";
    public static final String USER_DEGRADERATIORULE_RES = "userDegradeRATIORuleResource";

    public RTUserService() {
        initDegradeRTRule();
        //initDegradeRATIORule();
    }

    private static void initDegradeRTRule() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(USER_DEGRADERULE_RES);
        // set threshold rt, 100 ms
        rule.setCount(100);//资源的平均响应时间超过阈值100 ms，进入降级
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);//平均响应时间 (DEGRADE_GRADE_RT)
        rule.setTimeWindow(3);//持续降级的时间窗口3秒
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }


    private void initDegradeRATIORule() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(USER_DEGRADERATIORULE_RES);
        // 当资源的每秒异常总数占通过量的比值超过阈值（DegradeRule 中的 count）49% 之后，资源进入降级状态
        rule.setCount(0.49);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setTimeWindow(10);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }


    /**
     * 通过 @SentinelResource 注解定义资源并配置 blockHandler 和 fallback 函数来进行限流之后的处理
     *
     * @param id
     * @return
     */
    @SentinelResource(value = "userDegradeRuleResource",blockHandler = "blockHandlerForGetUser", fallback = "fallbackHandlerForGetUser")
    public User getUserByRTDegradeRule(int id) {
        try {
            //第5个请求开始超过阀值100ms
            if (id > 5) {
                TimeUnit.MILLISECONDS.sleep(150);
            }
            // 业务代码
            User user = new User();
            user.setId(id);
            user.setName("user-" + id);
            Thread.currentThread().sleep(20);
            //DB.InsertUser(user); //长耗时的工作
            return user;
        } catch (InterruptedException e) {
            return null;
        }
//        Entry entry = null;
//        try {
//
//            entry = SphU.entry(USER_DEGRADERULE_RES);
//            //第5个请求开始超过阀值100ms
//            if (id > 5) {
//                TimeUnit.MILLISECONDS.sleep(150);
//            }
//            // 业务代码
//            User user = new User();
//            user.setId(id);
//            user.setName("user-" + id);
//            Thread.currentThread().sleep(20);
//            //DB.InsertUser(user); //长耗时的工作
//            return user;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BlockException e) {
//            e.printStackTrace();
//            System.out.println(e + "[getUser] has been protected! id=" + id);
//            return new User("发生熔断，进入降级业务处理！");
//            //return new User("block user" + id);
//        } finally {
//            if (entry != null) {
//                entry.exit();
//            }
//        }
//        return null;
    }

    //@SentinelResource(blockHandler = "blockHandlerForGetUser", fallback = "fallbackHandlerForGetUser")
    public User getUserByRATIODegradeRule(int id) {
        initDegradeRATIORule();
        Entry entry = null;
        try {

            entry = SphU.entry(USER_DEGRADERATIORULE_RES);
            //第5个请求开始出现异常
            if (id > 5) {
                throw new RuntimeException("throw runtime ");
            }
            // 业务代码
            User user = new User();
            user.setId(id);
            user.setName("user-" + id);
            Thread.currentThread().sleep(100);
            //DB.InsertUser(user); //长耗时的工作
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e + "[getUser] has been protected! id=" + id);

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return new User("block user" + id);
    }


    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public User blockHandlerForGetUser(int id, BlockException ex) {
        return new User("熔断----block user" + id);
    }

    public User fallbackHandlerForGetUser(int id,Throwable throwable) {
        return new User("fallback user");
    }


}
