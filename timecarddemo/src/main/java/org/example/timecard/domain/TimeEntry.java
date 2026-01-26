package org.example.timecard.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"date", "jobCode"})
@Entity
@Table(name = "time_entry")
public class TimeEntry {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "job_code", nullable = false)
    private String jobCode;

    @Column(nullable = false)
    private int minutes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "timecard_id")
    private Timecard timecard;
}
