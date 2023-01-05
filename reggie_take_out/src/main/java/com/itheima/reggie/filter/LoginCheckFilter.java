package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
        //路径匹配器
        public static final AntPathMatcher  PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
        log.info("拦截到请求：{}",request.getRequestURI());
        //1、获取本次拦截的URL
        String requestURI = request.getRequestURI();
        //定义不需要处理的URL
        String[] urls=new String[]{
          "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",   //移动端发送短信
                "/user/login"   //移动端登录
        };
        //2、判断本次请求是否处理
        boolean check = check(urls, requestURI);
        //3、如果不需要处理，直接放行
        if (check){
            filterChain.doFilter(request,response);
            log.info("该url属于不用处理,放行url:"+requestURI);
            return;
        }
        //4-1、如果需要处理，判断登录状态，已登录，放行
        if (request.getSession().getAttribute("employee")!=null){
            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            log.info("已经登路,放行url"+requestURI+"该用户的id是："+request.getSession().getAttribute("employee"));
            return;
        }
        //4-2、如果需要处理，判断登录状态，已登录，放行
        if (request.getSession().getAttribute("user")!=null){
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            log.info("已经登路,放行url"+requestURI+"该用户的id是："+request.getSession().getAttribute("user"));
            return;
        }
        //5、如果未登录，返回JSON数据给前段
        log.info("未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 进行路径匹配
     * @param urls
     * @param requestURL
     * @return
     */
    public boolean check(String[] urls,String requestURL){
        for (String url:
             urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match){
                return true;
            }
        }
        return false;
    }
}
