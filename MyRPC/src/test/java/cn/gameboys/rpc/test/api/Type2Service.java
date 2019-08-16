package cn.gameboys.rpc.test.api;

import java.util.List;
import java.util.Map;


public interface Type2Service {
    List<Person> getTestPerson(String name, int num);
    
    Map<Integer,Person> mapTest(List<Person> list);
    
    
    
}
