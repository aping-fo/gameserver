package jetty;

import com.game.sdk.utils.XmlParser;
import com.game.sdk.erating.domain.RechargeData;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lucky on 2018/2/26.
 */
public class HelloHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Map map = new ConcurrentHashMap();
        map.putIfAbsent(1,1);
        /**
         * <pre>
         * 从 URL 里面得到传递过来的参数：
         *  http://localhost:8080/search?query=hello
         * 如果你需要传递更多的参数，可以这么写：
         *  http://localhost:8080/search?query=hello&name=ZhangLili
         * 从这里开始，你可以写自己的代码逻辑。
         *
         * [注意：GET方法的请求，URL 的最大长度是 1024个字节]
         * </pre>
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String content = reader.readLine();
        System.out.println(request.getParameterMap());
        RechargeData data = new RechargeData();
        XmlParser.xmlParser(content,data);
    }

    /**
     * <pre>
     * @param baseRequest
     * @param response
     * @param result 需要返回给客户的结果
     * @throws IOException
     * 将结果 result 返回给客户
     * </pre>
     */
    private void print(Request baseRequest, HttpServletResponse response,
                       String result) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(result);
    }

}