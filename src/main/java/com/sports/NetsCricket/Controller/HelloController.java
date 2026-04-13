package com.sports.NetsCricket.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HelloController {
	
	
	@GetMapping("/")
	public String greet(HttpServletRequest request) {
		return "welcome to spring"+request.getSession().getId();
	}

	
}
