package servlets;

import DAO.DAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;
import util.StringUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class UserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        String login = req.getParameter("login");
        String password = req.getParameter("password");

        if (login != null && password != null) {
            try {
                User user = (User) DAO.getObjectByParams(new String[]{"login", "password"}, new Object[]{login, password}, User.class);
                DAO.closeOpenedSession();
                if (user != null) {
                    String hash = StringUtil.generateHash();

                    user.setHash(hash);
                    DAO.updateObject(user);
                    Cookie cookie = new Cookie("hash", hash);
                    Cookie cookieId = new Cookie("id", String.valueOf(user.getId()));
                    cookie.setMaxAge(30 * 60);
                    cookieId.setMaxAge(30 * 60);
                    cookie.setPath("/");
                    cookieId.setPath("/");

                    resp.addCookie(cookie);
                    resp.addCookie(cookieId);

                    resp.getWriter().write(new ObjectMapper().writeValueAsString(user));
                }
            } catch (Exception e) {
                resp.setStatus(400);
                resp.getWriter().println(e.getMessage());
                System.out.println(e.getMessage());
            }
        } else {
            resp.setStatus(400);
            resp.getWriter().println("Login or password parameter is null");
        }
    }
}
