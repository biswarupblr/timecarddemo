package org.example.timecard.domain;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.timecard.exception.ValidationException;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.CascadeType;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "timecard",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "week_start"}
        ))
public class Timecard {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimecardStatus status;

    @Version
    private Long version;

    @OneToMany(
            mappedBy = "timecard",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TimeEntry> entries = new HashSet<>();


    //private String approvedBy;

    public void validateEntries() {
        List<String> errors = new ArrayList<>();

     Map<LocalDate, Integer> totalMinutesPerDay = entries.stream().collect(Collectors.groupingBy(TimeEntry :: getDate,
             Collectors.summingInt(TimeEntry :: getMinutes ) ));
        totalMinutesPerDay.forEach((date,total)  -> {
            if(total < 480  ){
                errors.add("Total minutes for " + date + " less than 8 hours: " + total);
            }
            if(total > 720  ){
                errors.add("Total minutes for " + date + " exceeds 12 hours: " + total);
            }
        } );

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }



}
