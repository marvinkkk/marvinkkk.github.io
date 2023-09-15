package com.myc.user.remote;

import java.util.List;

import javax.annotation.Resource;

import com.myc.netty.annotation.Remote;
import com.myc.netty.util.ResponseUtil;
import com.myc.user.model.User;
import com.myc.user.service.UserService;



@Remote
public class UserRemoteImpl implements UserRemote{
	@Resource
	private UserService userService;
	
	public Object saveUser(User user) {
		userService.save(user);
		return ResponseUtil.createSuccessResult(user);
	}
	
	public Object saveUsers(List<User> users) {
		userService.saveList(users);
		return ResponseUtil.createSuccessResult(users);
	}
}
