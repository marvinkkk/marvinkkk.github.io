package com.myc.pro_basic.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.myc.client.annotation.RemoteInvoke;
import com.myc.user.model.User;
import com.myc.user.remote.UserRemote;

@Service
public class BasicService {
	
	@RemoteInvoke
	private UserRemote userRemote;

	
	public void testSaveUser() {
		User u = new User();
		u.setId(1);
		u.setName("张三");
		Object response = userRemote.saveUser(u);
		System.out.println(JSONObject.toJSON(response));
	}

}
