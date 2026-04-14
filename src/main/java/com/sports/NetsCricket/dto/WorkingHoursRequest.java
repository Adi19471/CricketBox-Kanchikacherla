package com.sports.NetsCricket.dto;

public class WorkingHoursRequest {

    private String dayOfWeek;   // MONDAY
    private String startTime;   // 08:00
    private String endTime;     // 20:00
    private Integer slotDuration; // 30
    
    private Double morningPrice;
    private Double afternoonPrice;
    
	public String getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Integer getSlotDuration() {
		return slotDuration;
	}
	public void setSlotDuration(Integer slotDuration) {
		this.slotDuration = slotDuration;
	}
	
	public Double getMorningPrice() {
		return morningPrice;
	}

	public void setMorningPrice(Double morningPrice) {
		this.morningPrice = morningPrice;
	}

	public Double getAfternoonPrice() {
		return afternoonPrice;
	}

	public void setAfternoonPrice(Double afternoonPrice) {
		this.afternoonPrice = afternoonPrice;
	}
    
}
