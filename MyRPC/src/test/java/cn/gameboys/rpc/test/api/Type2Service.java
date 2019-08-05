package cn.gameboys.rpc.test.api;

import java.util.List;
import java.util.Map;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
public interface Type2Service {
    List<Person> getTestPerson(String name, int num);
    
    Map<Integer,Person> mapTest(List<Person> list);
    
    
    
}
