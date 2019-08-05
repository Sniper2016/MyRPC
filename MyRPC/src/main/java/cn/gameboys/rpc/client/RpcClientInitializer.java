package cn.gameboys.rpc.client;

import cn.gameboys.rpc.protocol.RpcDecoder;
import cn.gameboys.rpc.protocol.RpcEncoder;
import cn.gameboys.rpc.protocol.RpcRequest;
import cn.gameboys.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

	private RpcClientNode clientNode;

	public RpcClientInitializer(RpcClientNode clientNode) {
		super();
		this.clientNode = clientNode;
	}

	@Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder(RpcRequest.class));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcResponse.class));
        cp.addLast(new RpcClientHandler(clientNode));
    }
}
