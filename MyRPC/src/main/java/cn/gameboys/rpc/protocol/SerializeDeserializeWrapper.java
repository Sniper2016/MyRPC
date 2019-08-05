package cn.gameboys.rpc.protocol;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年7月31日
 */
public class SerializeDeserializeWrapper<T> {

	private T data;

	public static <T> SerializeDeserializeWrapper<T> builder(T data) {
		SerializeDeserializeWrapper<T> wrapper = new SerializeDeserializeWrapper<>();
		wrapper.setData(data);
		return wrapper;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
