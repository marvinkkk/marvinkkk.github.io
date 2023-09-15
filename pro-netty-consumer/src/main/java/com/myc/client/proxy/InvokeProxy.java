package com.myc.client.proxy;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.myc.client.annotation.RemoteInvoke;
import com.myc.client.core.TcpClient;
import com.myc.client.param.ClientRequest;
import com.myc.client.param.Response;
import com.myc.user.bean.User;

@Component
public class InvokeProxy implements BeanPostProcessor{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		
		//初始化之前获取所有属性
		Field[] fields = bean.getClass().getDeclaredFields();
		//遍历属性
		for(Field field : fields) {
			//判断RemoteInvoke注解是否存在
			if(field.isAnnotationPresent(RemoteInvoke.class)) {
				//可进行修改属性值
				field.setAccessible(true);
				
				//将field中的方法和接口的类型都添加进map
				final Map<Method, Class>methodClassMap = new HashMap<Method,Class>();
				putMethdClass(methodClassMap,field);
				
				//使用动态代理
				Enhancer enhancer = new Enhancer();
				//在哪些接口进行动态代理
				enhancer.setInterfaces(new Class[]{field.getType()});
				//回调
				enhancer.setCallback(new MethodInterceptor() {
					
					@Override
					//代码执行时,调用动态代理的时候拦截哪些方法
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy) throws Throwable {
						//采用netty客户端去调用服务器
						ClientRequest request = new ClientRequest();
						request.setCommand(methodClassMap.get(method).getName()+"."+method.getName());
						request.setContent(args[0]);
						Response resp = TcpClient.send(request);
		
						return resp;
					}
				});
				
				try {
					//修改属性值
					field.set(bean, enhancer.create());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		return bean;
	}

	/*
	 * 对属性的所有方法和接口的类型添加到一个map中
	 */
	private void putMethdClass(Map<Method, Class> methodClassMap, Field field) {
		
		Method[] methods = field.getType().getDeclaredMethods();
		for(Method m : methods) {
			methodClassMap.put(m, field.getType());
		}
		
		
	}

	@Override 
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
