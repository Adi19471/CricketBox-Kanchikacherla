package com.sports.NetsCricket.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sports.NetsCricket.entity.WorkingHours;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

	Optional<WorkingHours> findByDayOfWeek(String dayOfWeek);
}
