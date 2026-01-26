package org.example.timecard.service;

import org.example.timecard.domain.TimeEntry;
import org.example.timecard.domain.Timecard;
import org.example.timecard.domain.TimecardStatus;
import org.example.timecard.dto.TimeEntryRequestDTO;
import org.example.timecard.dto.TimecardRequestDTO;
import org.example.timecard.exception.InvalidStateException;
import org.example.timecard.exception.NotFoundException;
import org.example.timecard.repository.TimecardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@Transactional
public class TimecardService {

    private  final TimecardRepository repository;

    public TimecardService(TimecardRepository repository) {
        this.repository = repository;
    }

    public void createOrUpdateDraft(String employeeId, LocalDate weekStart, TimecardRequestDTO request) {


        Optional<Timecard> timecardOps = repository.findByEmployeeIdAndWeekStart(employeeId, weekStart );

        Timecard timecard = timecardOps.orElseGet(() -> {
            Timecard tc = new Timecard();
            tc.setEmployeeId(employeeId);
            tc.setWeekStart(weekStart);
            tc.setStatus(TimecardStatus.DRAFT);
            return tc;

        });

        if(timecard.getStatus() != TimecardStatus.DRAFT){
            throw new InvalidStateException(" Create or update timecard  allowed only in DRAFT mode ");
        }
        List<TimeEntryRequestDTO> timeEntryDTOList = request.getEntries();
        timecard.getEntries().clear();
        timeEntryDTOList.stream().forEach( dto -> {
            TimeEntry e = new TimeEntry();
            e.setDate(dto.getDate());
            e.setJobCode(dto.getJobCode());
            e.setMinutes(dto.getMinutes());
            e.setTimecard(timecard);
            timecard.getEntries().add(e);
        });

        timecard.validateEntries();
        repository.save(timecard);
    }

    public void submit(String employeeId, LocalDate weekStart) {
        Optional<Timecard> timecardOps = repository.findByEmployeeIdAndWeekStart(employeeId, weekStart );

        Timecard timecard = timecardOps.orElseThrow(() -> new NotFoundException("Timecard not found for employee Id : " + employeeId
                + "   Week Start " + weekStart ))                                   ;

        if(timecard.getStatus() != TimecardStatus.DRAFT){
            throw new InvalidStateException(" Submission allowed only in DRAFT mode ");
        }

        timecard.setStatus(TimecardStatus.SUBMITTED);
        repository.save(timecard);
    }

    public void approve(String employeeId, LocalDate weekStart) {
        Optional<Timecard> timecardOps = repository.findByEmployeeIdAndWeekStart(employeeId, weekStart );

        Timecard timecard = timecardOps.orElseThrow(() -> new NotFoundException("Timecard not found for employee Id : " + employeeId
                + "   Week Start " + weekStart ))                                   ;

        if(timecard.getStatus() != TimecardStatus.SUBMITTED){
            throw new InvalidStateException(" Approval  allowed only in SUBMITTED  mode ");
        }

        timecard.setStatus(TimecardStatus.APPROVED);
        repository.save(timecard);
    }

    @Transactional(readOnly = true)
    public List<?> findAll() {
        return repository.findAll();
    }
}
