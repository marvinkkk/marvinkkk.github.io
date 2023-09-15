package com.myc.client;

import java.util.ArrayList;
import java.util.List;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.myc.client.annotation.RemoteInvoke;
import com.myc.client.param.Response;
import com.myc.user.bean.User;
import com.myc.user.remote.UserRemote;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=RemoteInvokingTest.class)
@ComponentScan("com.myc")
public class RemoteInvokingTest {

	@RemoteInvoke
	private UserRemote userRemote;

	@Test
	public void testSaveUser() {
		User u = new User();
		u.setId(1);
		u.setName("张三");
		Response response = userRemote.saveUser(u);
		System.out.println(JSONObject.toJSON(response));
	}
	
	
	@Test
	public void testSaveUsers() {
		List<User> users = new ArrayList<User>();
		User u = new User();
		u.setId(1);
		u.setName("张三");
		users.add(u);
		userRemote.saveUsers(users);
	}
}
