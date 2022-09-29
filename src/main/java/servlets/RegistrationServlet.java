package servlets;

import DAO.DAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Date;

@WebServlet("/reg")
public class RegistrationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(req.getInputStream())) {
            User user = new ObjectMapper().readValue(bufferedInputStream, User.class);
            user.setDate(new Date());
            System.out.println(user);
            DAO.addObject(user);
            resp.getWriter().write(new ObjectMapper().writeValueAsString(user));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().println("This user already exists");
            System.out.println("This user already exists");
        } catch (Throwable e) {
            resp.setStatus(400);
            resp.getWriter().println(e.getMessage());
        }
    }
}
