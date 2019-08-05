package cn.gameboys.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
	private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

	private RpcClientNode rpcClientNode;

	public RpcClientHandler(RpcClientNode rpcClientNode) {
		this.rpcClientNode = rpcClientNode;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		this.rpcClientNode.onChannelRegistered(ctx.channel());
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		this.rpcClientNode.onReceivedRsp(response);
	}
	

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("client caught exception", cause);
		ctx.close();
	}
}
