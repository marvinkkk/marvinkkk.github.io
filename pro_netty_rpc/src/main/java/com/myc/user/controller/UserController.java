package com.myc.user.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;

import com.myc.netty.util.Response;
import com.myc.netty.util.ResponseUtil;
import com.myc.user.bean.User;
import com.myc.user.service.UserService;

@Controller
public class UserController {
	
	@Resource
	private UserService userService;
	
	public Response saveUser(User user) {
		userService.save(user);
		return ResponseUtil.createSuccessResult(user);
	}
	
	public Response saveUsers(List<User> users) {
		userService.saveList(users);
		return ResponseUtil.createSuccessResult(users);
	}

}
