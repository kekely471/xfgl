package com.tonbu.support.filter;

import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ShiroPermissionsFilter extends PermissionsAuthorizationFilter {

    Logger logger = LogManager.getLogger(ShiroPermissionsFilter.class.getName());
    /**
     * shiro认证perms资源失败后回调方法
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws IOException
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        logger.info("----------权限控制-------------");
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String header = httpServletRequest.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest" == header;
        if (isAjax) {//如果是ajax返回指定格式数据
            logger.info("----------AJAX请求拒绝-------------");
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json");
            ResultEntity r=new ResultEntity();
            r.setCode(-1);
            r.setMsg("权限不足!! 禁止访问!!");
            //返回禁止访问json字符串
            httpServletResponse.getWriter().write(JsonUtils.toJson(r));
        } else {//如果是普通请求进行重定向
            logger.info("----------普通请求拒绝-------------");
            httpServletResponse.sendRedirect("error/error-403");
        }
        return false;
    }
}
