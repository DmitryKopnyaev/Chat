package filter;


import DAO.DAO;
import model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(value = "/*", asyncSupported = true)
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Cookie[] cookies = request.getCookies();
        String value = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("hash"))
                    value = cookie.getValue();
            }
        }

        System.out.print(request.getRequestURI());

        //URL Запроса/переадресации на Servlet входа
        String loginURI = request.getContextPath() + "/login";
        String registerURI = request.getContextPath() + "/reg";
        //Если сессия ранее создана
        boolean loginRequest = request.getRequestURI().contains(loginURI);
        boolean registerRequest = request.getRequestURI().contains(registerURI);
        //Если запрос пришел со страницы с входом или сессия не пуста даем добро следовать дальше
        //Если нет ридирект на страницу входа
        if (request.getRequestURI().endsWith("js") || request.getRequestURI().endsWith("css") || loginRequest || registerRequest
                || value != null && DAO.getObjectByParam("hash", value, User.class) != null) {
            System.out.print(" in if filter\n");
            filterChain.doFilter(request, response);
        } else {
            System.out.print(" else filter\n");
            response.sendRedirect(loginURI + ".html");
        }
    }

    @Override
    public void destroy() {

    }
}
