package org.jboss.server;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/test")
public class BenchmarkServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        long time = System.currentTimeMillis();
        for(int i = 0; i < 1000; ++i) {
            SessionBeanRemote remote = null;
            try {
                remote = (SessionBeanRemote) new InitialContext().lookup("java:global/remote-ejb-stresstest-ear/remote-ejb-stresstest-ejb/SimpleStatefulSessionBean!org.jboss.server.SessionBeanRemote");
            } catch (NamingException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < 100; ++j) {
                remote.businessMethod("test");
            }
            remote.businessMethodDone();
        }
        resp.getWriter().write("Total time " + (System.currentTimeMillis() - time));
    }
}
