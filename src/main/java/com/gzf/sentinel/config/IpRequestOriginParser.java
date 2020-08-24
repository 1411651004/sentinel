package com.gzf.sentinel.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: sentinel
 * @description: 获取客户端origin-ip
 * @author: Gaozf
 * @create: 2020-08-12 16:52
 **/
public class IpRequestOriginParser implements RequestOriginParser {
    @Override
    public String parseOrigin(HttpServletRequest httpServletRequest) {
        System.out.println("origin ："+httpServletRequest.getRemoteAddr());
        return httpServletRequest.getRemoteAddr();
    }
}
