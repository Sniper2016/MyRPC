package cn.gameboys.rpc.protocol;

import java.util.List;

import cn.gameboys.rpc.expection.RpcErrorEnum;
import cn.gameboys.rpc.expection.RpcExpection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class RpcDecoder extends ByteToMessageDecoder {

	private Class<?> genericClass;

	public RpcDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		if (in.readableBytes() < 4) {
			// throw new RpcExpection(RpcErrorEnum.INIT_ERROR);
			throw new RpcExpection(RpcErrorEnum.EN_DE_CODE_ERROR, "");
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		/*
		 * if (dataLength <= 0) { ctx.close(); }
		 */
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);

		// Object obj = SerializationUtil.deserialize(data, genericClass);
		// Object obj = JsonUtil.deserialize(data,genericClass); // Not use this, have
		// some bugs
		Object obj = ProtostuffUtils.deserialize(data, genericClass);

		out.add(obj);
	}

}
