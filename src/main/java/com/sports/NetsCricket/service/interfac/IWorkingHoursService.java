package com.sports.NetsCricket.service.interfac;

import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.dto.WorkingHoursRequest;

public interface IWorkingHoursService {

	public Response setWorkingHours(WorkingHoursRequest request);
}
