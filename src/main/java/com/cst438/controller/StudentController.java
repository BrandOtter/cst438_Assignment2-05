package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {


    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    TermRepository termRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;


    // studentId will be temporary until Login security is implemented
    //example URL  /transcript?studentId=19803

    // student gets transcript showing list of all enrollments
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);

        if(enrollments == null){
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "student ID is invalid");
        }

        List<EnrollmentDTO> transcript = new ArrayList<>();
        for (Enrollment e : enrollments) {
            EnrollmentDTO dto = new EnrollmentDTO(e.getEnrollmentId(), e.getGrade(), e.getUser().getId(), e.getUser().getName(),
                    e.getUser().getEmail(), e.getSection().getCourse().getCourseId(), e.getSection().getSecId(),
                    e.getSection().getSectionNo(), e.getSection().getBuilding(), e.getSection().getRoom(),
                    e.getSection().getTimes(), e.getSection().getCourse().getCredits(), e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester());

            transcript.add(dto);
        }
        return transcript;
    }

    // student gets a list of their enrollments for the given year, semester
    // user must be student
    // studentId will be temporary until Login security is implemented
    @GetMapping("/enrollments")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            @RequestParam("studentId") int studentId) {


        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);

        if(enrollments == null){
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "section with given year and semester not found");
        }

        List<EnrollmentDTO> schedule = new ArrayList<>();
        for (Enrollment e : enrollments) {
            EnrollmentDTO dto = new EnrollmentDTO(e.getEnrollmentId(), e.getGrade(), e.getUser().getId(), e.getUser().getName(),
                    e.getUser().getEmail(), e.getSection().getCourse().getCourseId(), e.getSection().getSecId(),
                    e.getSection().getSectionNo(), e.getSection().getBuilding(), e.getSection().getRoom(),
                    e.getSection().getTimes(), e.getSection().getCourse().getCredits(), e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester());

            schedule.add(dto);
        }
        return schedule;
    }


    // student adds enrollment into a section
    // user must be student
    // return EnrollmentDTO with enrollmentId generated by database
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam(value = "studentId", defaultValue = "3") int studentId ) {

        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if(e != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student already enrolled");
        }

        e = new Enrollment();

        User student = userRepository.findById(studentId).orElse(null);
        if (student==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student id not found");
        }

        e.setUser(student);
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        if(section == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "section number not found");
        }

        Date today = new Date();
        if (today.before(section.getTerm().getAddDate()) || today.after(section.getTerm().getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not within the enrollment period");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(section.getTerm().getYear(),
                section.getTerm().getSemester(), studentId);


        e.setSection(section);
        e.setGrade(null);
        enrollmentRepository.save(e);


        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(e.getEnrollmentId(), e.getGrade(), e.getUser().getId(), e.getUser().getName(),
                e.getUser().getEmail(), e.getSection().getCourse().getCourseId(), e.getSection().getSecId(),
                e.getSection().getSectionNo(), e.getSection().getBuilding(), e.getSection().getRoom(),
                e.getSection().getTimes(), e.getSection().getCourse().getCredits(), e.getSection().getTerm().getYear(),
                e.getSection().getTerm().getSemester());

        return enrollmentDTO;

    }

    // student drops a course
    // user must be student
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);

        if (enrollmentOpt.isEmpty()) {
            return;
        }

        Enrollment enrollment = enrollmentOpt.get();

        Date dropDeadline = enrollment.getSection().getTerm().getDropDeadline();
        Date today = new Date();

        if (today.after(dropDeadline)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot drop course after the drop deadline");
        }
        
        enrollmentRepository.deleteById(enrollmentId);
    }
}