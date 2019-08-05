package cn.gameboys.rpc.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.gameboys.rpc.protocol.ProtostuffUtils;
import cn.gameboys.rpc.protocol.SerializeDeserializeWrapper;

/**
 * 
 * Description: 序列化测试
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月5日
 */
public class ProtostuffTest {

	public static void main(String[] args) {

		// 性能测试
		performanceTest();

		// 测试向后兼容--》新增字段,字段名不一样但是顺序和类型一样就ok
		compatibilityTest1();
		compatibilityTest2();

		// 特殊类型序列化测试
		specialTypeTest();
	}

	/**
	 * 性能测试
	 */
	public static void performanceTest() {
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "butioyprotostuff");
		map.put("key2", "protostuffprotostuff");
		map.put("key3", "serializeprotostuffprotostuffpr");
		map.put("key4", "protostuffprotostuffprotostuffprotostuff");
		map.put("key5", "serializeprotostuffprotostuffprotostuffprotostuff");
		map.put("key6", "protostuffprotostuffprotostuffprotostuffprotostuffprotostuff");
		map.put("key7", "serializeprotostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuff");
		map.put("key8", "protostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuff");
		map.put("key9", "serializeprotostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuffprotostuff");
		Map<String, Object> resultMap = null;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			byte[] byteArr = ProtostuffUtils.serialize(map);
			resultMap = ProtostuffUtils.deserialize(byteArr, Map.class);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("性能测试 useTime:" + (endTime - startTime));
		System.out.println(resultMap);
		// 结论是使用了700ms，100000次序列化，0.007ms每次序列化，速度飞起

	}

	/**
	 * 兼容性测试
	 */
	public static void compatibilityTest1() {
		User user = new User();
		user.setId(666);
		user.setName("sniper old");
		byte[] byteArr = ProtostuffUtils.serialize(user);
		User2 user2 = ProtostuffUtils.deserialize(byteArr, User2.class);
		System.out.println(user2);

	}

	/**
	 * 兼容性测试
	 */
	public static void compatibilityTest2() {
		User2 user2 = new User2();
		user2.setI(555);
		user2.setN("sniper user2 old");
		user2.setA(18);
		byte[] byteArr = ProtostuffUtils.serialize(user2);
		User user = ProtostuffUtils.deserialize(byteArr, User.class);
		System.out.println(user);

	}

	/**
	 * 特殊类型测试
	 */
	public static void specialTypeTest() {
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "butioy");
		map.put("key2", "protostuff");
		map.put("key3", "serialize");
		SerializeDeserializeWrapper<?> wrapper = SerializeDeserializeWrapper.builder(map);
		byte[] serializeBytes = ProtostuffUtils.serialize(wrapper);
		SerializeDeserializeWrapper<?> deserializeWrapper = ProtostuffUtils.deserialize(serializeBytes, SerializeDeserializeWrapper.class);
		System.out.println("反序列化map对象：" + deserializeWrapper.getData());

		List<User> list = new ArrayList<User>();
		list.add(new User(1, "sniper1"));
		list.add(new User(2, "sniper2"));
		list.add(new User(3, "sniper3"));
		SerializeDeserializeWrapper<?> wrapper2 = SerializeDeserializeWrapper.builder(list);
		byte[] serializeBytes2 = ProtostuffUtils.serialize(wrapper2);
		SerializeDeserializeWrapper<?> deserializeWrapper2 = ProtostuffUtils.deserialize(serializeBytes2, SerializeDeserializeWrapper.class);
		System.out.println("反序列化list对象：" + deserializeWrapper2.getData());

	}

}
