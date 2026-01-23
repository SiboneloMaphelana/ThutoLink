package com.sbuda.sbuda.controllers;

import com.sbuda.sbuda.DTOS.StudentRequest;
import com.sbuda.sbuda.models.Student;
import com.sbuda.sbuda.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    //ADD A STUDENT
    @PostMapping("/add-student")
    public Student addStudent(@RequestBody StudentRequest student){
        return studentService.createStudent(student);
    }

    //GET ALL STUDENTS
    @GetMapping
    public List<Student> getAllStudents(){
        return studentService.getStudents();
    }

    //GET SPECIFIC STUDENT
    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Integer id){
        return studentService.getStudent(id);
    }

    //DELETE SPECIFIC STUDENT
    @DeleteMapping("/delete-student/{id}")
    public void deleteStudent(@PathVariable Integer id){
        studentService.removeStudent(id);
    }

    //UPDATE STUDENT
//    @PatchMapping("/update-student")
//    public Student u

}
