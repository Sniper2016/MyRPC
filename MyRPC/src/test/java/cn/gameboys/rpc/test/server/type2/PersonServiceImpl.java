package cn.gameboys.rpc.test.server.type2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.gameboys.rpc.server.RpcService;
import cn.gameboys.rpc.test.api.Person;
import cn.gameboys.rpc.test.api.Type2Service;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
@RpcService(Type2Service.class)
public class PersonServiceImpl implements Type2Service {

    @Override
    public List<Person> getTestPerson(String name, int num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(i,"type1-"+i, name));
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
