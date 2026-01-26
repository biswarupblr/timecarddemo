package org.example.timecard.repository;

import org.example.timecard.domain.Timecard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TimecardRepository extends JpaRepository<Timecard, Long> {
    Optional<Timecard> findByEmployeeIdAndWeekStart(
            String employeeId,
            LocalDate weekStart
    );
}
