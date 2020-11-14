package org.littlewings.infinispan.session;

import java.io.IOException;
import java.io.WriteAbortedException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/counter")
public class CounterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        Counter counter = (Counter) session.getAttribute("counter");

        if (counter == null) {
            counter = new Counter();
            session.setAttribute("counter", counter);
        }

        int current = counter.increment();

        resp.getWriter().write(String.format("current value = %d%n", current));
    }
}
