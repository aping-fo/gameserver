package com.game.module.admin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import com.game.SysConfig;

public class ManagerServer {

	// 启动后台管理的webservice
	public static void start() throws Exception {
		if (!SysConfig.mangerService) {
			return;
		}
		 EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	        EventLoopGroup workerGroup = new NioEventLoopGroup();
	            ServerBootstrap b = new ServerBootstrap();
	            b.group(bossGroup, workerGroup)
	             .channel(NioServerSocketChannel.class)
	             .childHandler(new ChannelInitializer<SocketChannel>() {
	            	 @Override
	            	protected void initChannel(SocketChannel ch) throws Exception {
	            		ChannelPipeline p = ch.pipeline();
	            		p.addLast(new HttpServerCodec());
						 p.addLast(new HttpObjectAggregator(65535));
						p.addLast(new ManagerHandler());
	            		
	            	}
				});
	            b.bind(SysConfig.gmPort);
	}
}
