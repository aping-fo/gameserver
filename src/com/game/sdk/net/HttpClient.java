package com.game.sdk.net;

import com.game.SysConfig;
import com.game.sdk.utils.EncoderHandler;
import com.server.util.ServerLogger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

public class HttpClient {

    private static final String SIGN = "sign";
    private static RequestConfig requestConfig = null;

    static {
        // 设置http的状态参数
        requestConfig = RequestConfig.custom().setSocketTimeout(10000) // 取数据超时
                .setConnectTimeout(10000) // 设置连接超时
                .setConnectionRequestTimeout(10000).build();
    }

    /**
     * 发送POST 请求,数据二进制
     *
     * @param url
     * @param fn
     * @param data
     * @throws Exception
     */
    public static String sendPostRequest(String url, String fn, byte[] data) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // CloseableHttpClient httpclient =
        // HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder build = MultipartEntityBuilder.create();
            build.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .seContentType(ContentType.MULTIPART_FORM_DATA)
                    .addBinaryBody(fn, data);
            // 增加时间戳
            httpPost.setEntity(build.build());
            response = httpclient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            String json = EntityUtils.toString(httpEntity);
            EntityUtils.consume(httpEntity);
            return json;
        } finally {
            if (response != null)
                response.close();
            httpclient.close();
        }
    }

    /**
     * 发送POST 请求
     *
     * @param url
     * @param params
     * @param params
     * @throws Exception
     */
    public static String sendPostRequest(String url, Map<String, String> params) throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            Set<Entry<String, String>> set = params.entrySet();
            for (Entry<String, String> s : set) {
                nameValuePairs.add(new BasicNameValuePair(s.getKey(), s.getValue()));
            }
            // 增加时间戳
            nameValuePairs.add(new BasicNameValuePair("time", System.currentTimeMillis() + ""));
            String sign = buildSign(nameValuePairs);
            nameValuePairs.add(new BasicNameValuePair(SIGN, sign));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, Charset.forName("utf-8")));
            response = httpclient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            String json = EntityUtils.toString(httpEntity);
            System.out.println(json);
            EntityUtils.consume(httpEntity);
            return json;
        } finally {
            if (response != null)
                response.close();
            httpclient.close();
        }
    }


    /**
     * 发送POST 请求
     *
     * @throws Exception
     */
    public static String sendPostRequest(String content) throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse response = null;
        try {
            String url = "http://" + SysConfig.eratingHost + "/agip";
            //String url = "http://127.0.0.1:9801";
            HttpPost httpPost = new HttpPost(url); //"http://113.208.129.53:14820/agip"
            httpPost.setHeader("Pragma", "no-cache");
            httpPost.setHeader("Accept", "*/*");

            StringEntity entity = new StringEntity(content, "UTF-8");
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            String json = EntityUtils.toString(httpEntity,"UTF-8");
            ServerLogger.warn("\r\n"+json);
            EntityUtils.consume(httpEntity);
            return json;
        } finally {
            if (response != null)
                response.close();
            httpclient.close();
        }
    }

    /**
     * 发送GET 请求,可以进行异步回调
     *
     * @param params
     * @return
     */
    public static void sendGetRequest(String url, Map<String, String> params, IHttpHandler handler) throws Exception {
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse response = null;
        try {
            long time = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder(url).append("?time=" + time);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            // 增加时间戳
            nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(time)));
            Set<Entry<String, String>> set = params.entrySet();
            for (Entry<String, String> s : set) {
                nameValuePairs.add(new BasicNameValuePair(s.getKey(), s.getValue()));
                sb.append("&").append(s.getKey()).append("=").append(s.getValue());
            }
            String sign = buildSign(nameValuePairs);
            sb.append("&").append(SIGN).append("=").append(sign);

            HttpGet httpget = new HttpGet(sb.toString());
            response = httpclient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String json = EntityUtils.toString(httpEntity);
            if (handler != null)
                handler.handlerHttpRequest(json);
            EntityUtils.consume(httpEntity);
        } finally {
            if (response != null)
                response.close();
            httpclient.close();
        }
    }

    private static String buildSign(List<NameValuePair> nameValuePairs) throws Exception {
        Collections.sort(nameValuePairs, new UrlSort());
        StringBuilder sb = new StringBuilder("SGAME");
        for (NameValuePair nv : nameValuePairs) {
            sb.append("&").append(nv.getName()).append("=")
                    // .append(URLEncoder.encode(nv.getValue().trim(),"utf-8"));
                    .append(nv.getValue().trim());
        }
        String sign = EncoderHandler.sign(sb.toString());
        return sign;
    }

    // 请求字符串排序
    public static class UrlSort implements Comparator<NameValuePair> {
        @Override
        public int compare(NameValuePair o1, NameValuePair o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("content", "测试公告长度,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告"
                + "测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告"
                + "测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告"
                + "测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告，测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告,测试公告");
        params.put("times", 1 + "");
        params.put("startTime", System.currentTimeMillis() + 10 + "");
        params.put("endTime", System.currentTimeMillis() + 100 + "");
        params.put("serverId", "1");
        HttpClient.sendPostRequest("http://192.168.7.200:20010/admin/notice", params); // ,"000000000".getBytes()
        //HttpClient.sendPostRequest("http://113.208.129.53:14820/agip"); // ,"000000000".getBytes()  // ,
        // params,b.build().toByteArray()
    }
}
