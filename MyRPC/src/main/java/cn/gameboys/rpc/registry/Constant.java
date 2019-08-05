package cn.gameboys.rpc.registry;

/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public interface Constant {

    int ZK_SESSION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
