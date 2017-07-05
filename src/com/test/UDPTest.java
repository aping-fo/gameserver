package com.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.List;

public class UDPTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		UDPServer server = new UDPServer(9999);
		server.launch();
		Protocol pro = new Protocol();
		pro.id = 1;
		pro.content = 100;
		server.send("localhost", 9999, pro);
	}

}

class UDPServer {

	private int port;
	private Channel channel;

	public UDPServer(int port) {
		this.port = port;
	}

	public void launch() {
//		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();

		EventExecutorGroup group = new DefaultEventExecutorGroup(5);

		try {
			Bootstrap boot = new Bootstrap();
			boot.group(workGroup).channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_BROADCAST, true)
					.handler(new ChannelInitializer<DatagramChannel>() {

						@Override
						protected void initChannel(DatagramChannel channel)
								throws Exception {
							ChannelPipeline line = channel.pipeline();
							line.addLast(new Decoder());
							line.addLast("e3", new Encoder3());
							line.addLast("e", new Encoder());
							line.addLast("e2", new Encoder2());
							line.addLast(group, new Handler());
						}
					});
			channel = boot.bind(port).sync().channel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void send(String ip, int port, Protocol protocol){
		
		channel.writeAndFlush(new UDPResponse(new InetSocketAddress(ip, port), protocol));
	}
	
}

class Decoder extends MessageToMessageDecoder<DatagramPacket> {

	@Override
	protected void decode(ChannelHandlerContext arg0, DatagramPacket packet,
			List<Object> arg2) throws Exception {
		// TODO Auto-generated method stub
		ByteBuf buf = (ByteBuf) packet.copy().content();
		Protocol protocol = new Protocol();
		protocol.decode(buf);
		arg2.add(protocol);
	}

}

@Sharable
class Encoder2 extends MessageToMessageEncoder<UDPResponse> {

	public static ThreadLocal<InetSocketAddress> address = new ThreadLocal<InetSocketAddress>();
	@Override
	protected void encode(ChannelHandlerContext ctx, UDPResponse arg1,
			List<Object> arg2) throws Exception {

		address.set(arg1.getAddress());
		arg2.add(arg1.getProtocol());		
	}

}

class Encoder extends MessageToMessageEncoder<Protocol> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Protocol arg1,
			List<Object> arg2) throws Exception {
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
		arg1.encode(buf);

		arg2.add(buf);
		buf.retain();
	}

}



class Encoder3 extends MessageToMessageEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf arg1,
			List<Object> arg2) throws Exception {
		DatagramPacket packet = new DatagramPacket(arg1, Encoder2.address.get());

		arg2.add(packet);
		System.out.println(arg1.refCnt());
	}

}



class Handler extends SimpleChannelInboundHandler<Protocol> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Protocol protocol)
			throws Exception {
		System.out.println("Receive msg : " + protocol);
		Protocol pro = new Protocol();
		pro.id = 2;
		pro.content = 3;
		//ctx.writeAndFlush(pro);
	}

}

class Protocol {
	public int id;
	public int content;

	public void decode(ByteBuf buf) {
		id = buf.readInt();
		content = buf.readInt();
	}

	public void encode(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(content);
	}

	public String toString() {
		return "protocol id=" + id + ", content=" + content;
	}
}

class UDPResponse{
	private InetSocketAddress address;
	private Protocol protocol;
	
	public UDPResponse(InetSocketAddress address, Protocol protocol){
		this.address = address;
		this.protocol = protocol;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public Protocol getProtocol() {
		return protocol;
	}
	
	
}
