package com.game.sdk.utils;

import com.game.SysConfig;
import com.game.util.CommonUtil;
import com.game.util.HttpRequestUtil;
import com.game.util.JsonUtils;
import com.server.util.ServerLogger;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 乐起sdk 工具类
 */
public class LuckySdkUtil {
    public enum Type {REGISTER, CHECK_TOKEN}

    private static final String OAUTH_SIGNATURE_METHOD = "md5";
    private static final String OAUTH_VERSION = "1.0";
    private static final String SUCCESS = "1";

    /**
     * 向SDK服务器注册游戏逻辑服
     */
    public static void register() {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("projectid", SysConfig.oauthkey);
            request.put("serverid", String.valueOf(SysConfig.serverId));
            //充值回调地址
            String url = "http://" + String.valueOf(SysConfig.host) + ":" + String.valueOf(SysConfig.gmPort) + "/lucky/recharge";
            ServerLogger.warn("注册地址：" + url);
            request.put("url", url);
            signature(SysConfig.registerurl, request, Type.REGISTER);
            String result = sendPost(SysConfig.registerurl, request);
            Map<String, String> response = JsonUtils.string2Map(result, String.class, String.class);
            if (response == null) {
                ServerLogger.warn("解析注册返回信息失败");
                return;
            }
            if (!"0".equals(response.get("code"))) {
                ServerLogger.warn("注册失败:" + response.get("message"));
            }
        } catch (Exception ex) {
            ServerLogger.err(ex, "注册失败");
        }
    }

    /**
     * 验证登录token的合法性
     *
     * @param token
     * @param udid
     * @return 如果验证成功则返回平台用户名，否则返回NULL
     */
    public static String checkToken(String token, String udid) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("token", token);
            request.put("udid", udid);
            signature(SysConfig.checktokenurl, request, Type.CHECK_TOKEN);
            String result = sendPost(SysConfig.checktokenurl, request);

            Map<String, Object> response = JsonUtils.string2Map(result, String.class, Object.class);
            if (response == null) {
                ServerLogger.warn("解析验证token返回信息失败");
                return null;
            }
            if (0 != (int) response.get("code")) {
                ServerLogger.warn("验证失败:" + response.get("message"));
            }
            Map<String, String> data = (Map<String, String>) response.get("data");//JsonUtils.string2Map(response.get("data"), String.class, String.class);
            if (data == null) {
                ServerLogger.warn("解析验证token返回data数据信息失败");
                return null;
            }
            return data.get("ppusername");
        } catch (Exception ex) {
            ServerLogger.err(ex, "验证token失败");
        }
        return null;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数
     * @return 所代表远程资源的响应结果
     */
    private static String sendPost(String url, Map<String, String> param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            int tryCount = 5;
            while (conn.getResponseCode() != 200 && tryCount-- > 0) {
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                }
                conn = (HttpURLConnection) realUrl.openConnection();
            }
            if (conn.getResponseCode() != 200) {
                return null;
            }
            // 设置通用的请求属性
            conn.setRequestProperty("ContentType", "text/xml;charset=utf-8");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            for (Map.Entry<String, String> entry : param.entrySet()) {
                result.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            out.print(result.deleteCharAt(result.length() - 1).toString());
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            result.setLength(0);
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            ServerLogger.err(e, "发送 POST 请求出现异常！url=" + url);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ServerLogger.err(ex, "发送 POST 请求出现异常！");
            }
        }
        return result.toString();
    }


    /**
     * 为发出的请求加上签名
     *
     * @param url     服务端地址
     * @param request 请求参数数组
     */
    private static void signature(String url, Map<String, String> request, Type type) {
        if (request == null || request.isEmpty()) {
            return;
        }
        int now = (int) (System.currentTimeMillis() / 1000);
        request.put("oauth_nonce", String.valueOf(now));
        request.put("oauth_timestamp", String.valueOf(now));
        switch (type) {
            case REGISTER:
                request.put("oauth_consumer_key", SysConfig.registerkey);
                break;
            case CHECK_TOKEN:
                request.put("oauth_consumer_key", SysConfig.oauthkey);
                break;
        }
        request.put("oauth_version", OAUTH_VERSION);
        request.put("oauth_signature_method", OAUTH_SIGNATURE_METHOD);
        request.put("oauth_signature", buildSignature(request, url, type));
    }

    /**
     * 签名校验
     * 检查request中的签名，判断请求是否伪造
     *
     * @param request 请求数据，一般是POST或GET数组
     * @param url     地址
     * @return boolean
     */
    public static boolean checkSignature(Map<String, String> request, String url, Type type) {
        return request != null && !request.isEmpty() && request.get("oauth_nonce") != null
                && request.get("oauth_timestamp") != null && request.get("oauth_consumer_key") != null
                && request.get("oauth_signature_method") != null && request.get("oauth_signature") != null
                && request.get("oauth_signature").equals(buildSignature(request, url, type));
    }

    /**
     * 对请求参数签名
     *
     * @param request 参数数组
     * @param url     服务端接口地址
     * @return string 签名
     */
    private static String buildSignature(Map<String, String> request, String url, Type type) {
        //对参数进行排序，避免参数顺序不同，造成生成的签名不同
        List<String> keys = new ArrayList<>(request.keySet());
        Collections.sort(keys);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (String key : keys) {
                if (key.equals("oauth_signature")) {
                    continue;
                }
                System.out.println(request.get(key));
                System.out.println(URLEncoder.encode(request.get(key), "utf-8"));
                stringBuilder.append(URLEncoder.encode(key, "utf-8")).append("=")
                        .append(URLEncoder.encode(request.get(key), "utf-8")).append("&");
            }

            String baseString = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
            baseString = URLEncoder.encode(baseString.replace("+", "%20"), "utf-8");

            stringBuilder.setLength(0);
            switch (type) {
                case REGISTER:
                    stringBuilder.append(URLEncoder.encode(SysConfig.registersecret, "utf-8"));
                    break;
                case CHECK_TOKEN:
                    stringBuilder.append(URLEncoder.encode(SysConfig.oauthsecret, "utf-8"));
                    break;
            }
            stringBuilder.append("&").append("POST&").append(URLEncoder.encode(url, "utf-8")).append("&").append(baseString);
            System.out.println(stringBuilder.toString());
            return CommonUtil.md5(stringBuilder.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            ServerLogger.err(e, "对请求参数签名, 编码失败");
        }
        return "";
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        /**
         File file = new File("c:/pay.log");
         try{
         BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
         String s = null;
         double rmbs = 0;//付费总额
         Set<Integer> paySet = new HashSet<Integer>();//付费账号
         int payCount = 0;//付费人次
         Set<String> orderSet = new HashSet<String>();//订单号
         int reOrderCount = 0;
         while((s = br.readLine())!=null){//使用readLine方法，一次读一行
         //String str = "[WARN ] 04-16 17:29:08  com.game.module.admin.ManagerHandler.channelRead0(ManagerHandler.java:102): manger handle:%s,%s,pay,status=1&order_sn=201804161729070000487100&uid=4134&amount=6.0000&currency=rmb&targetamount=60.00&product_id=26&platment=openqqyyb&game_area=1&apply_time=2018-04-16 17:29:07&exts=1718001&oauth_nonce=1523870948&oauth_timestamp=1523870948&oauth_consumer_key=1000&oauth_signature_method=md5&oauth_version=1.0&oauth_signature=adb6832c0de629b2d7466925cba47ef4";
         int index = s.indexOf("status");
         if(index < 0) continue;
         String str = s.substring(index, s.length());
         Map<String, String> map = toMap(str);
         if(map == null || map.get("uid") == null){
         System.out.println();
         }
         int uid = Integer.parseInt(map.get("uid"));
         if(!paySet.contains(uid)){
         paySet.add(uid);
         }
         float rmb = Float.parseFloat(map.get("amount"));
         rmbs += rmb;
         String order_sn = map.get("order_sn");
         if(!orderSet.contains(order_sn)){
         orderSet.add(order_sn);
         payCount++;
         }else{
         reOrderCount++;
         }
         }
         System.out.println("rmbs=" + rmbs);
         System.out.println("付费人数：" + paySet.size());
         System.out.println("付费人次:" + payCount);
         System.out.println("重复单号：" + reOrderCount);
         br.close();
         }catch(Exception e){
         e.printStackTrace();
         }
         */


    }

    public static Map<String, String> toMap(String str) {
        Map<String, String> map = new HashMap<String, String>();
        String[] arr1 = str.split("&");
        for (String arr : arr1) {
            String[] arr2 = arr.split("=");
            if (arr2 == null || arr2.length != 2) {
                continue;
            }
            map.put(arr2[0], arr2[1]);
        }
        return map;
    }

    //ios token验证
    public static boolean checkIosToken(String userToken, String memId) {
        try {
            if (StringUtils.isBlank(userToken)) {
                ServerLogger.warn("tonek为空，tonken=" + userToken);
            }
            StringBuffer request = new StringBuffer();
            request.append("app_id=").append(SysConfig.gameIdIOS + "")
                    .append("&mem_id=").append(memId)
                    .append("&user_token=").append(userToken);

            StringBuffer sign = new StringBuffer();
            sign.append("app_id=").append(SysConfig.gameIdIOS)
                    .append("&mem_id=").append(memId)
                    .append("&user_token=").append(userToken)
                    .append("&app_key=").append(SysConfig.gameKeyIOS);

            String signString = CommonUtil.md5(sign.toString().getBytes("utf-8"));

            request.append("&sign=").append(signString);

            String result = HttpRequestUtil.sendPost(SysConfig.checktokenurlIos, request.toString());

            Map<String, Object> response = JsonUtils.string2Map(result, String.class, Object.class);
            if (response == null) {
                ServerLogger.warn("解析验证token返回信息失败");
                return false;
            }

            if (SUCCESS.equals(response.get("status"))) {
                return true;
            } else {
                ServerLogger.warn("验证失败:" + response.get("msg"));
                return false;
            }
        } catch (Exception ex) {
            ServerLogger.err(ex, "验证token失败");
            return false;
        }
    }
}
