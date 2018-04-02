package jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * Created by lucky on 2018/2/26.
 */
public class JettyExampleServer {
    /**
     * <pre>
     * @param args
     * </pre>
     */
    public static void main(String[] args) {
        try {
            // 进行服务器配置
            Server server = new Server(8083);
            ContextHandler context = new ContextHandler();
            // 设置搜索的URL地址
            context.setContextPath("/");
            context.setResourceBase(".");
            context.setClassLoader(Thread.currentThread().getContextClassLoader());
            server.setHandler(context);
            context.setHandler(new HelloHandler());
            // 启动服务器
            server.start();
           // server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
