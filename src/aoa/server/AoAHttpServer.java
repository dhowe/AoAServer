package aoa.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

/*
 * Valid URLS:
 * 
 *   http://localhost:8080/aoa/?cmd=$CMD 
 */
public class AoAHttpServer extends Thread
{
    protected String contextPath = "/aoa";
    protected String rootDir = ".";
    protected int port = 8080;
    
    private AoAMessageServer sender;
    
    public AoAHttpServer(AoAMessageServer ams) {
        this.sender = ams;        
    }
    
    public void run() 
    {
        Server httpServer = new Server();
        Connector connector=new SelectChannelConnector();
        connector.setPort(port);
        httpServer.setConnectors(new Connector[]{connector});
        
        ContextHandler context = new ContextHandler();
        context.setContextPath(contextPath);
        context.setResourceBase(rootDir);
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        httpServer.setHandler(context);
        
        context.setHandler(new HttpCommandHandler());
        
        try {
            httpServer.start();
            httpServer.join();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
     
    class HttpCommandHandler extends AbstractHandler {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
          throws IOException, ServletException
        {
            Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
            base_request.setHandled(true);
            
            String cmd = request.getParameter("cmd");      
            System.out.println("AoAHttpServer.handle("+cmd+")");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            response.getWriter().println("<h1>CMD="+cmd+"</h1>");
            sender.sendMessage("<"+cmd+" millis='"+System.currentTimeMillis()+"'/>\n");
        }
    }

    public String getContextPath() {
        return contextPath;
    }
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    public String getRootDir() {
        return rootDir;
    }
    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }
    public int getPort() {
        return port;
    }
    
}// end