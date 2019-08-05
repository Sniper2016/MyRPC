package cn.gameboys.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

	private RpcServer rpcServer;

	public RpcHandler(RpcServer rcpServer) {
		this.rpcServer = rcpServer;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final RpcRequest request) throws Exception {
		this.rpcServer.onReceivedMsg(request, ctx);
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("server caught exception", cause);
		ctx.close();
	}
}
