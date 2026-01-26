package org.example.timecard.controller;

import jakarta.validation.Valid;
import org.example.timecard.domain.Timecard;
import org.example.timecard.dto.TimecardRequestDTO;
import org.example.timecard.service.TimecardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timecards")
public class TimeCardController {

    private final TimecardService service;

    public TimeCardController(TimecardService service) {
        this.service = service;
    }

    /**
     * Create or update a DRAFT timecard
     */
    @PostMapping("/{employeeId}/{weekStart}")
    public ResponseEntity<?> createOrUpdateDraft(
            @PathVariable String employeeId,
            @PathVariable LocalDate weekStart,
            @Valid @RequestBody TimecardRequestDTO request ) {
        service.createOrUpdateDraft(employeeId, weekStart, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Submit a DRAFT timecard
     */
    @PostMapping("/{employeeId}/{weekStart}/submit")
    public ResponseEntity<Void> submit(
            @PathVariable String employeeId,
            @PathVariable LocalDate weekStart ) {
        service.submit(employeeId, weekStart);
        return ResponseEntity.ok().build();
    }

    /**
     * Approve a SUBMITTED timecard
     */
    @PostMapping("/{employeeId}/{weekStart}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable String employeeId,
            @PathVariable LocalDate weekStart  ) {
        service.approve(employeeId, weekStart);
        return ResponseEntity.ok().build();
    }

    /**
     * List all timecards
     */
    @GetMapping
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }


}
