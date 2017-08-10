package com.game.module.admin;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.game.util.Context;
import com.game.util.StringUtil;
import com.server.util.ServerLogger;

public class ManagerHandler extends ChannelInboundHandlerAdapter {

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(false); 

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (!(msg instanceof HttpRequest)) {
			return;
		}
		HttpRequest request = (HttpRequest) msg;

		String url=null;
		try {
			url = URLDecoder.decode(request.getUri(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			ServerLogger.err(e, "http url decode err!");
			return;
		}
		if (url.equals("/favicon.ico")) {
			HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
			Channel channel = ctx.channel();
			channel.write(res);
			channel.flush();
			channel.disconnect();
			channel.close();
			return;
		}

		// http://113.105.250.53:8010/send_sys_bulletin?&msg_type=1&content=11&time=1354674212&flag=0f77e1b5bec5730afb3f9c56035f356b
		int indexEnd = url.indexOf("?");
		if (indexEnd < 0) {
			indexEnd = url.length();
		}
		String action = url.substring(url.indexOf("/") + 1, indexEnd);
		if (action == null || "".equals(action)) {
			return;
		}
//		String ip = CommonUtil.getIp(ctx.channel().remoteAddress());
		//充值检查一下白名单
		
//		if(!SysConfig.debug&&action.equals("pay")){
//			if(!ip.equals(SysConfig.sdkServer)){
//				ServerLogger.warn("err sdk server ip:"+ip);
//				return;
//			}
//		}

		String content = url.substring(indexEnd + 1);
		// 兼容post和get
		if (request.getMethod() == HttpMethod.POST) {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request, Charset.forName("UTF-8"));
			StringBuilder post = new StringBuilder();
			for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
				if (data.getHttpDataType() == HttpDataType.Attribute) {
					Attribute attribute = (Attribute) data;
					String key = attribute.getName();
					String value = "";
					try {
						value = attribute.getValue();
					} catch (Exception e) {
						ServerLogger.err(e, "get http values err:" + key + content);
					}
					post.append(attribute.getName()).append("=").append(value).append("&");
				}
			}
			content = String.format("%s&%s", content, post.substring(0, post.length() - 1));
		}

		ByteBuf buffer = Unpooled.buffer(2048);
		Map<String, String> params = new HashMap<String, String>();
		if (content != null && !"".equals(content)) {
			params = StringUtil.str2StrMap(content, "&", "=");
		}

		ServerLogger.warn("manger handle:%s,%s", action, content);

		// http://192.168.52.151:20001/online_user_info_list?search=121&user_id%22%3A%221%22%2C%22account%22%3A%22test%22%2C%22sex%22%3A%221%22%7D&order=%7B%22user_id%22%3A%22desc%22%7D&page=%7B%22start_num%22%3A%220%22%2C%22per_page%22%3A%2210%22%7D&time=1359089998&flag=c0bdd2bd7bf67f3f2b4fd8de3fc7837d
		// String requestUrl =
		// "http://127.0.0.1:8080/send_sys_bulletin?content=%E6%B5%8B%E8%AF%95&copy_text=&link_color=&link_text=&msg_type=1&text_color=&time=1361771080&v=2.0&flag=5C42CFF2B0B83C4833642D524F632F75";
		String result = null;
		try {
			params.put("act", action);
			result = Context.getManagerService().handle(params);
		} catch (Exception e) {
			ServerLogger.err(e, "handle manager err!" + content);
		}
		if (result != null) {
			buffer.writeBytes(result.getBytes(Charset.forName("utf-8")));
		}
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK, buffer);
		response.headers().set(CONTENT_TYPE, "text/plain");
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

		Channel channel = ctx.channel();
		channel.write(response);
		channel.flush();
		channel.disconnect();
		channel.close();
	}
}
