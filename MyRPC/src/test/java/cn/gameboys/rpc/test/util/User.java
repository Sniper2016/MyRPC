package cn.gameboys.rpc.test.util;
/** 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日 
*/
public class User {
	private int id;
	private String name;

	public User() {

	}

	public User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	// private int age;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + "]";
	}
}
