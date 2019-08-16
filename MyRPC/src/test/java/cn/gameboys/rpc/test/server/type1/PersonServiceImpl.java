package cn.gameboys.rpc.test.server.type1;

import cn.gameboys.rpc.server.RpcService;
import cn.gameboys.rpc.test.api.Person;
import cn.gameboys.rpc.test.api.Type1Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RpcService(Type1Service.class)
public class PersonServiceImpl implements Type1Service {
	@Override
	public List<Person> getTestPerson(String name, int num) {
		List<Person> persons = new ArrayList<>(num);
		for (int i = 0; i < num; ++i) {
			persons.add(new Person(i, "type2-" + i, name));
		}
		return persons;
	}

	@Override
	public Map<Integer, Person> mapTest(List<Person> list) {
		Map<Integer, Person> map = new HashMap<Integer, Person>();
		for (Person person : list) {
			map.put(person.getUserID(), person);
		}
		return map;
	}
}
