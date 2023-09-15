package com.myc.netty.medium;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;



import com.alibaba.fastjson.JSONObject;
import com.myc.netty.handler.param.ServerRequest;
import com.myc.netty.util.Response;

public class Media {

	public static Map<String, BeanMethod>beanMap;
	static {
		beanMap = new HashMap<String, BeanMethod>();
	}
	
	private static Media m = null;
	private Media() {
		
	}
	public static Media newInstance() {
		if(m == null) {
			m = new Media();
		}
		return m;
	}
	
	//反射出列业务代码,关键
	public Response process(ServerRequest request) {
		Response result =null;
		try {
			String command = request.getCommand();
			BeanMethod beanMethod = beanMap.get(command);
			if (beanMethod == null) {
				return null;
			}
			
			Object bean = beanMethod.getBean();
			Method m = beanMethod.getMethod();
			//调用方法的参数类型和内容
			Class paramType = m.getParameterTypes()[0];
			Object content = request.getContent();
			Object args = JSONObject.parseObject(JSONObject.toJSONString(content), paramType);
			
			result =  (Response) m.invoke(bean,args);
			result.setId(request.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
}
