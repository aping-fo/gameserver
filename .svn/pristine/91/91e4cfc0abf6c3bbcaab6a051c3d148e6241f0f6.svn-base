package com.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPTest2 {
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		Bootstrap b = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		b.group(group).channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.handler(new UDPSeverHandler());

		b.bind(9999).sync().channel().closeFuture().await();
	}
}

class UDPSeverHandler extends SimpleChannelInboundHandler<DatagramPacket> {


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelHandlerAdapter#channelRegistered(io.netty.channel
	 * .ChannelHandlerContext)
	 */
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
		// System.out.println("I got it!");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket packet)
			throws Exception {
		// TODO Auto-generated method stub
		ByteBuf buf = (ByteBuf) packet.copy().content();
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req, "UTF-8");
		System.out.println(body);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelHandlerAdapter#channelReadComplete(io.netty.channel
	 * .ChannelHandlerContext)
	 */

}
