package com.sports.NetsCricket.service.impl;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.dto.WorkingHoursRequest;
import com.sports.NetsCricket.entity.WorkingHours;
import com.sports.NetsCricket.repo.WorkingHoursRepository;
import com.sports.NetsCricket.service.interfac.IWorkingHoursService;

@Service
public class WorkingHoursService implements IWorkingHoursService{
	
	@Autowired
	private WorkingHoursRepository workingHoursRepository;

	@Override
	public Response setWorkingHours(WorkingHoursRequest request) {

	    Response response = new Response();

	    try {

	        String day = request.getDayOfWeek().toUpperCase();

	        WorkingHours wh = workingHoursRepository
	                .findByDayOfWeek(day)
	                .orElse(new WorkingHours());

	        wh.setDayOfWeek(day);
	        wh.setStartTime(LocalTime.parse(request.getStartTime()));
	        wh.setEndTime(LocalTime.parse(request.getEndTime()));
	        wh.setSlotDuration(request.getSlotDuration());

	        workingHoursRepository.save(wh);

	        response.setStatusCode(200);
	        response.setMessage("Working hours saved successfully");
	        response.setData(wh);

	    } catch (Exception e) {
	        response.setStatusCode(500);
	        response.setMessage(e.getMessage());
	        e.printStackTrace();
	    }

	    return response;
	}
}
