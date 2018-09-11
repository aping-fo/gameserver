package com.game.sdk;

import com.game.SysConfig;
import com.game.sdk.utils.WebHandler;
import com.game.sdk.web.SdkServlet;
import com.game.util.ClassUtil;
import com.server.util.ServerLogger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.Servlet;
import java.util.Set;

/**
 * Created by lucky on 2018/2/27.
 */
public class SdkServer {
    public static void main(String[] args) {
        start();
    }

    public static void start() {
        try {
            // 进行服务器配置
            Server server = new Server(SysConfig.sdkPort);

            HttpConfiguration https_config = new HttpConfiguration();
            https_config.setSecureScheme("https");
            https_config.setSecurePort(8443);
            https_config.setOutputBufferSize(32768);
            https_config.addCustomizer(new SecureRequestCustomizer());

            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath("config/jetty.jks");
            sslContextFactory.setKeyStorePassword("1537095695707");
            sslContextFactory.setKeyManagerPassword("1537095695707");

            ServerConnector httpsConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory,"http/1.1"),
                    new HttpConnectionFactory(https_config));
            httpsConnector.setPort(8443);
            httpsConnector.setIdleTimeout(500000);
            server.addConnector(httpsConnector);


            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            context.setResourceBase(".");
            server.setHandler(context);

            Set<Class<?>> classes = ClassUtil.getClasses(SdkServlet.class.getPackage());
            for (Class<?> clazz : classes) { //加载所有的servlet
                WebHandler annotation = clazz.getAnnotation(WebHandler.class);
                if (annotation != null) {
                    String path = annotation.url();
                    Servlet servlet = (Servlet) clazz.newInstance();
                    context.addServlet(new ServletHolder(servlet), path);
                }
            }
            // 启动服务器
            server.start();
            ServerLogger.warn("sdk server start on " + SysConfig.sdkPort);
        } catch (Throwable e) {
            ServerLogger.err(e, "sdk server 启动失败");
        }
    }
}
