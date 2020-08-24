package com.gzf.sentinel.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @program: sentinel
 * @description: 获取spring容器bean的工具类
 * @author: Gaozf
 * @create: 2020-08-13 16:12
 **/
@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    protected static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T  getBean(String name) {
        //name表示其他要注入的注解name名
        return (T) context.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }
}
