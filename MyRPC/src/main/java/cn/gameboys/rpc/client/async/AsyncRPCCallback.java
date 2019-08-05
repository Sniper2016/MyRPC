package cn.gameboys.rpc.client.async;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;


/**
 * 
* Description: 异步回调注解
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AsyncRPCCallback {
    String value();
}
