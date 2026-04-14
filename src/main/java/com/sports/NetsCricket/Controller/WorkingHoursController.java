package com.sports.NetsCricket.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.dto.WorkingHoursRequest;
import com.sports.NetsCricket.service.impl.WorkingHoursService;

@RestController
@RequestMapping("/WorkingHours")
public class WorkingHoursController {
	
	@Autowired
	private WorkingHoursService workingHoursService;
	
	@PostMapping("/working-hours")
	public ResponseEntity<Response> setWorkingHours(@RequestBody WorkingHoursRequest request) {

	    Response response = workingHoursService.setWorkingHours(request);
	    return ResponseEntity.status(response.getStatusCode()).body(response);
	}

}
