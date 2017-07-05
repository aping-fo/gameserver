package com.test.testnetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyTestHandler extends ChannelInboundHandlerAdapter {
	
	// 保存所有连接的Channel，用于消息广播
	private static ChannelGroup allChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	// Channel连接成功时，加入allChannels
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		allChannels.add(ctx.channel());
		super.channelActive(ctx);
	}

	@SuppressWarnings("unused")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ByteBuf data = (ByteBuf) msg;
		// 前面4个字节存储长度
		if (data.readableBytes() < 4) {
			return;
		}
		data.markReaderIndex();
		// 解析出消息体的长度
		
		int length = data.readInt();
		// 消息体长度不够，继续等待
		if (data.readableBytes() < length) {
			data.resetReaderIndex();
			return;
		}
		// 解析出消息体
		byte[] dataBytes = new byte[length];
		data.readBytes(dataBytes);
		
		String str = new String(dataBytes);
		
		ByteBuf sendData = Unpooled.buffer();
		sendData.writeBytes(dataBytes);
		//广播
		allChannels.writeAndFlush(sendData);
	}

}
