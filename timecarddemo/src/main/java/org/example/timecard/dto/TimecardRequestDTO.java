package org.example.timecard.dto;



import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class TimecardRequestDTO {

    @NotEmpty
    private List<TimeEntryRequestDTO> entries;
}
