package org.example.timecard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
@Data
public class TimeEntryRequestDTO {

    @NotNull
    private LocalDate date;

    @NotBlank
    private String jobCode;

    @Positive
    private int minutes;
}
