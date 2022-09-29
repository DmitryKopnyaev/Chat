package servlets;

import DAO.DAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Message;
import model.User;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@WebServlet(value = "/message", asyncSupported = true)
public class MessageServlet extends HttpServlet {
    //пользователь онлайн пока в мапе есть его id
    private Map<Long, List<AsyncContext>> asyncContextMap = new ConcurrentHashMap<>();
    private BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private boolean run;


    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("run");
            while (run) {
                try {
                    Message message = messages.take();//м-д блокирующий поток
                    System.out.println("message = " + message);
                    for (List<AsyncContext> list : asyncContextMap.values())
                        for (AsyncContext context : list)
                            sendMessage(context, message);
                } catch (InterruptedException | IOException ignored) {
                }
            }
        }
    });

    private void sendMessage(AsyncContext asyncContext, Message message) throws IOException {
        PrintWriter writer = asyncContext.getResponse().getWriter();
        writer.println("data: " + message);
        writer.println();
        writer.flush();
    }

    @Override
    public void destroy() {
        run = false;
        messages.clear();
        asyncContextMap.clear();
    }

    @Override
    public void init() {
        thread.start();
        run = true;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");

        String id = req.getParameter("id_user");
        String text = req.getParameter("text");
        if (id != null && text != null) {
            User user = (User) DAO.getObjectById(Long.parseLong(id), User.class);
            DAO.closeOpenedSession();
            Message message = new Message(text, user, new Date());
            DAO.addObject(message);
            System.out.println("MESSAGE = " + message);
            messages.add(message);
            resp.getWriter().write(message.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");

        if (req.getHeader("Accept").equals("text/event-stream")) {
            resp.setContentType("text/event-stream");
            resp.setHeader("Connection", "keep-alive");

            long id = 0;
            Cookie[] cookies = req.getCookies();
            for (Cookie c : cookies)
                if (c.getName().equals("id"))
                    id = Long.parseLong(c.getValue());

            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(300000L);//время м-у startAsync() и timeOut
            long finalId = id;
            asyncContext.addListener(new AsyncListener() {
                @Override
                public void onComplete(AsyncEvent asyncEvent) {
                    deleteContext(asyncEvent, finalId);
                    System.out.println("doGet -> " + new Date().getMinutes() + ":" + new Date().getSeconds() + " [onComplete -> id : " + finalId + "] : " + asyncContextMap);
                }

                @Override
                public void onTimeout(AsyncEvent asyncEvent) {
                    deleteContext(asyncEvent, finalId);
                    System.out.println("doGet -> " + new Date().getMinutes() + ":" + new Date().getSeconds() + " [onTimeout -> id : " + finalId + "] : " + asyncContextMap);
                }

                @Override
                public void onError(AsyncEvent asyncEvent) {
                    deleteContext(asyncEvent, finalId);
                    System.out.println("doGet -> " + new Date().getMinutes() + ":" + new Date().getSeconds() + " [onError -> id : " + finalId + "] : " + asyncContextMap);
                }

                @Override
                public void onStartAsync(AsyncEvent asyncEvent) {
                    System.out.println("doGet -> " + new Date().getMinutes() + ":" + new Date().getSeconds() + " [onStartAsync -> id : " + finalId + "] : " + asyncContextMap);
                }
            });

            asyncContextMap.putIfAbsent(id, new ArrayList<>());
            asyncContextMap.get(id).add(asyncContext);

            User user = (User) DAO.getObjectById(finalId, User.class);
            if (!user.isConnected()) user.setConnected(true);
            DAO.closeOpenedSession();
            DAO.updateObject(user);

            System.out.println("doGet -> " + new Date().getMinutes() + ":" + new Date().getSeconds() + " ADD id = " + finalId + " connected = true] : " + asyncContextMap);
        } else {
            //TODO получить все сообщения из базы
            resp.setContentType("application/json;charset=utf-8");
            System.out.println("doGet -> " + new Date().getMinutes() + ":" + new Date().getSeconds() + " : getAllObjects");
            List<Message> allObjects = DAO.getAllObjects(Message.class);
            allObjects.sort(Comparator.comparingLong(o -> o.getDate().getTime()));
            DAO.closeOpenedSession();
            resp.getWriter().write(new ObjectMapper().writeValueAsString(allObjects));

        }
    }

    private void deleteContext(AsyncEvent asyncEvent, long finalId) {
        if (asyncContextMap.get(finalId) != null) {
            asyncContextMap.get(finalId).remove(asyncEvent.getAsyncContext());
            if (asyncContextMap.get(finalId).size() == 0) {
                asyncContextMap.remove(finalId);
                User user = (User) DAO.getObjectById(finalId, User.class);
                if (user.isConnected()) user.setConnected(false);
                DAO.closeOpenedSession();
                DAO.updateObject(user);
            }
        }
    }
}
