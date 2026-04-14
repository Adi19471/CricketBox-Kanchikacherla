package com.sports.NetsCricket.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sports.NetsCricket.dto.BookingRequest;
import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.entity.Booking;
import com.sports.NetsCricket.entity.User;
import com.sports.NetsCricket.entity.WorkingHours;
import com.sports.NetsCricket.repo.BookingRepository;
import com.sports.NetsCricket.repo.UserRepository;
import com.sports.NetsCricket.repo.WorkingHoursRepository;
import com.sports.NetsCricket.service.interfac.IBookingService;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    // 🔥 PRICE PER HOUR (you can move to DB later)
    private static final double PRICE_PER_HOUR = 500;

    // ✅ CREATE BOOKING
    @Override
    public Response createBooking(BookingRequest request) {

        Response response = new Response();

        try {

            // ✅ 1. Get logged-in user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ 2. Validate time
            long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

            if (minutes <= 0) {
                throw new RuntimeException("Invalid time range");
            }

            if (minutes % 60 != 0) {
                throw new RuntimeException("Only full hour bookings allowed");
            }

            // ✅ 3. Check overlap
            List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                    request.getBookingDate(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            if (!overlaps.isEmpty()) {
                throw new RuntimeException("Slot already booked");
            }

            // ✅ 4. Calculate hours & amount
            double totalHours = minutes / 60.0;
            double amount = totalHours * PRICE_PER_HOUR;

            // ✅ 5. Save booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBookingDate(request.getBookingDate());
            booking.setStartTime(request.getStartTime());
            booking.setEndTime(request.getEndTime());
            booking.setTotalHours(totalHours);
            booking.setAmount(amount);
            booking.setStatus("PENDING");
            booking.setPaymentStatus("PENDING");

            bookingRepository.save(booking);

            response.setStatusCode(200);
            response.setMessage("Booking created successfully");
            response.setData(booking);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    // ✅ GET MY BOOKINGS
    @Override
    public Response getMyBookings() {

        Response response = new Response();

        try {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Booking> bookings = bookingRepository.findByUserId(user.getId());

            response.setStatusCode(200);
            response.setData(bookings);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    // ✅ CANCEL BOOKING (ADMIN ONLY)
    @Override
    public Response cancelBooking(Long bookingId) {

        Response response = new Response();

        try {

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus("CANCELLED");

            bookingRepository.save(booking);

            response.setStatusCode(200);
            response.setMessage("Booking cancelled successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
    
    
    @Override
    public Response getBookedSlots(LocalDate bookingDate) {

        Response response = new Response();

        try {

            // ✅ 12-hour formatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

            // 1. Get working hours
            String day = bookingDate.getDayOfWeek().name();

            WorkingHours wh = workingHoursRepository.findByDayOfWeek(day)
                    .orElseThrow(() -> new RuntimeException("Working hours not set"));

            LocalTime start = wh.getStartTime();
            LocalTime end = wh.getEndTime();
            int duration = wh.getSlotDuration();

            // 2. Get booked slots
            List<Booking> bookings = bookingRepository.findByBookingDate(bookingDate);

            // 3. Generate ALL slots (with session + price)
            List<Map<String, Object>> allSlots = new ArrayList<>();

            LocalTime temp = start;

            while (temp.isBefore(end)) {

                LocalTime next = temp.plusMinutes(duration);

                Map<String, Object> slot = new HashMap<>();

                slot.put("startTime", temp.format(formatter));
                slot.put("endTime", next.format(formatter));

                // ✅ session
                String session = temp.isBefore(LocalTime.NOON) ? "MORNING" : "AFTERNOON";
                slot.put("session", session);

                // ✅ price from DB
                double price = temp.isBefore(LocalTime.NOON)
                        ? (wh.getMorningPrice() != null ? wh.getMorningPrice() : 0)
                        : (wh.getAfternoonPrice() != null ? wh.getAfternoonPrice() : 0);

                slot.put("price", price);

                allSlots.add(slot);

                temp = next;
            }

			// 4. Booked slots (12-hour format)
			List<Map<String, Object>> bookedSlots = bookings.stream().map(b -> {

				Map<String, Object> map = new HashMap<>();

				map.put("startTime", b.getStartTime().format(formatter));
				map.put("endTime", b.getEndTime().format(formatter));

				// ✅ session (optional but good for UI)
				String session = b.getStartTime().isBefore(LocalTime.NOON) ? "MORNING" : "AFTERNOON";
				map.put("session", session);

				// 🔥 IMPORTANT: price = 0
				map.put("price", 0);

				return map;

			}).toList();

            
			// 5. Available slots (with session + price retained)
            List<Map<String, Object>> availableSlots = allSlots.stream()
                    .filter(slot -> bookings.stream().noneMatch(b ->
                            isOverlap(
                                    LocalTime.parse((String) slot.get("startTime"), formatter),
                                    LocalTime.parse((String) slot.get("endTime"), formatter),
                                    b.getStartTime(),
                                    b.getEndTime()
                            )
                    ))
                    .toList();

            // 6. Final response
            Map<String, Object> result = new HashMap<>();
            result.put("booked", bookedSlots);
            result.put("available", availableSlots);

            response.setStatusCode(200);
            response.setMessage("Slots fetched successfully");
            response.setData(result);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

	public boolean isOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {

		return !(end1.isBefore(start2) || start1.isAfter(end2));
	}
}
