package com.myc.user.remote;

import java.util.List;

import javax.xml.ws.Response;

import com.myc.user.model.User;

public interface UserRemote {
	
	
	public Object saveUser(User user);
	public Object saveUsers(List<User> users);
	
	
}
