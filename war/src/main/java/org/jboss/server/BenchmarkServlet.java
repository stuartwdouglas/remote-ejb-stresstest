package org.jboss.server;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

@WebServlet(urlPatterns = "/test")
public class BenchmarkServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String type = req.getParameter("type");
        Integer rep = Integer.parseInt(req.getParameter("reps"));
        String name;
        if ("tx".equals(type)) {
            doTxTest(rep, resp);
            return;
        }
        if ("slsb".equals(type)) {
            name = "java:global/remote-ejb-stresstest-ear/remote-ejb-stresstest-ejb/SimpleStatelessSessionBean!org.jboss.server.SessionBeanRemote";
        } else {
            name = "java:global/remote-ejb-stresstest-ear/remote-ejb-stresstest-ejb/SimpleStatefulSessionBean!org.jboss.server.SessionBeanRemote";
        }
        long time = System.currentTimeMillis();
        for (int i = 0; i < rep; ++i) {
            SessionBeanRemote remote = null;
            SessionBeanRemote remote2 = null;
            try {
                remote = (SessionBeanRemote) new InitialContext().lookup(name);
                remote2 = (SessionBeanRemote) new InitialContext().lookup(name);
            } catch (NamingException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < 100; ++j) {
                remote.businessMethod("test");
            }
            remote.businessMethodDone();
            remote2.businessMethodDone();
        }
        resp.getWriter().write("Total time " + (System.currentTimeMillis() - time));
    }

    private void doTxTest(Integer rep, HttpServletResponse resp) {
        long time = System.currentTimeMillis();
        try {
            UserTransaction tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            for (int i = 0; i < rep; ++i) {
                tx.begin();
                tx.commit();
            }
            resp.getWriter().write("Total time " + (System.currentTimeMillis() - time));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
