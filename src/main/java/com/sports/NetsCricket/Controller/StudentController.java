package com.sports.NetsCricket.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class StudentController {
	
	private List<Student> students = new ArrayList<>(List.of(
			new Student(1, "vamsi", 29),
			new Student(2, "chandu", 26)));

	@GetMapping("/students")
	public List<Student> getStudents(){
		return students;
	}
	
	@GetMapping("csrf-token")
	public CsrfToken getCsrfToken(HttpServletRequest request) {
		return (CsrfToken)request.getAttribute("_csrf");
	};
	
	@PostMapping("/students")
	public Student getStudents(@RequestBody Student student){
		students.add(student);
		return student;
	}
}
