package com.myc.user.remote;

import java.util.List;

import com.myc.client.param.Response;
import com.myc.user.bean.User;

public interface UserRemote {
	
	
	public Response saveUser(User user);
	public Response saveUsers(List<User> users);
	
	
}
