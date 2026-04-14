package com.sports.NetsCricket.service.impl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

	        // ✅ 12-hour formatter
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

	        // ✅ Parse 12-hour input
	        LocalTime startTime = LocalTime.parse(request.getStartTime().toUpperCase(), formatter);
	        LocalTime endTime = LocalTime.parse(request.getEndTime().toUpperCase(), formatter);

	        // ✅ Set basic fields
	        wh.setDayOfWeek(day);
	        wh.setStartTime(startTime);
	        wh.setEndTime(endTime);
	        wh.setSlotDuration(request.getSlotDuration());

	        // 🔥 NEW: Set pricing
	        wh.setMorningPrice(request.getMorningPrice());
	        wh.setAfternoonPrice(request.getAfternoonPrice());

	        workingHoursRepository.save(wh);

	        response.setStatusCode(200);
	        response.setMessage("Working hours & pricing saved successfully");
	        response.setData(wh);

	    } catch (Exception e) {
	        response.setStatusCode(500);
	        response.setMessage(e.getMessage());
	        e.printStackTrace();
	    }

	    return response;
	}
}
