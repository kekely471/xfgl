package com.tonbu.support.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.support.helper.JwtTokenHelper;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ApiInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) {
        try{
            String token = request.getParameter("userId");
            if(token==null||token.trim().equals("")){
                responseMsg(response,ResultEntity.newInstance().error("5000","token为空"));
                return false;
            }

            if(jwtTokenHelper.isTokenExpired(token)){
                responseMsg(response,ResultEntity.newInstance().error("5001","token失效"));
                return false;
            }
            jwtTokenHelper.parseToken(token);
        }catch (JwtException e){
            responseMsg(response,ResultEntity.newInstance().error("5002","token无效"));
            return false;
        }

        return true;
    }



    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }


    private void responseMsg(HttpServletResponse response,ResultEntity re){

        //不符合条件的给出提示信息，并转发到登录页面
        Object json = JSONObject.toJSON(re);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.append(json.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis()/1000);
    }
}