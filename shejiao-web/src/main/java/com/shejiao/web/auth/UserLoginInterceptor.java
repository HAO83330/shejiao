package com.shejiao.web.auth;

import com.shejiao.common.constant.TokenConstant;
import com.shejiao.common.constant.UserConstant;
import com.shejiao.common.enums.ResultCodeEnum;
import com.shejiao.common.exception.ServiceException;
import com.shejiao.common.utils.JwtUtils;
import com.shejiao.common.utils.WebUtils;
import com.shejiao.common.validator.myVaildator.noLogin.NoLoginIntercept;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.service.IWebAuthUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author shejiao
 */
@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {

    /**
     * token拦截验证
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        // 检查请求路径是否包含 "/web" 或 "/ai"，但排除管理系统的请求
        if ((requestURI.contains("/web") || requestURI.contains("/ai")) && !requestURI.contains("/system/ai")) {
            String accessToken = request.getHeader(TokenConstant.ACCESS_TOKEN);
            log.info("[拦截器] 请求路径: {}, accessToken: {}", requestURI, accessToken);
            
            //获取方法处理器
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            NoLoginIntercept noLoginIntercept =
                    handlerMethod.getMethod()//这一步是获取到我们要访问的方法
                            //然后根据我们制定的自定义注解的Class对象来获取到对应的注解
                            .getAnnotation(NoLoginIntercept.class);

            if (noLoginIntercept != null) {
                log.info("[拦截器] 方法有@NoLoginIntercept注解，放行");
                if (!StringUtils.isEmpty(accessToken)) {
                    setLocalUser(accessToken, request);
                }
                return true;
            }

            //判断token不为空
            if (!StringUtils.isEmpty(accessToken)) {
                log.info("[拦截器] 有token，设置用户ID");
                setLocalUser(accessToken, request);
                log.info("[拦截器] 当前用户ID: {}", AuthContextHolder.getUserId());
                return true;
            }
            log.warn("[拦截器] 没有token，拒绝访问: {}", requestURI);
            throw new ServiceException("用户未登录，请先登录", ResultCodeEnum.TOKEN_FAIL.getCode());
        }
        return true;
    }

    private void setLocalUser(String accessToken, HttpServletRequest request) {
        boolean flag = JwtUtils.checkToken(accessToken);
        if (!flag) {
            throw new ServiceException(ResultCodeEnum.TOKEN_EXIST.getMessage(), ResultCodeEnum.TOKEN_EXIST.getCode());
        }
        String userId = JwtUtils.getUserId(accessToken);
        // 检查用户状态
        ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext(request);
        if (applicationContext != null) {
            IWebAuthUserService webAuthUserService = applicationContext.getBean(IWebAuthUserService.class);
            WebUser user = webAuthUserService.getById(userId);
            if (user != null && "1".equals(user.getStatus())) {
                throw new ServiceException("用户已被停用，无法访问", ResultCodeEnum.TOKEN_FAIL.getCode());
            }
        }
        AuthContextHolder.setUserId(userId);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
//        log.info("清除UserID");
        AuthContextHolder.removeUserId();
    }
}
