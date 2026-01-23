package com.sbuda.sbuda.services;

import com.sbuda.sbuda.DTOS.StudentRequest;
import com.sbuda.sbuda.models.Program;
import com.sbuda.sbuda.models.Student;
import com.sbuda.sbuda.repositories.ProgramRepository;
import com.sbuda.sbuda.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProgramRepository programRepository;


    //ADD A STUDENT
    public Student createStudent(StudentRequest request){

        //FIND PROGRAM FROM DATABASE
        Program program = programRepository.findById(request.getProgramId()).orElseThrow(() -> new RuntimeException("Id not found"));

        //CREATE NEW STUDENT
        Student student = new Student();
        student.setName(request.getName());
        student.setAge(request.getAge());
        student.setEmail(request.getEmail());
        student.setProgram(program);
        return studentRepository.save(student);
    }

    //RETRIEVE ALL STUDENTS
    public List<Student> getStudents(){
        return studentRepository.findAll();
    }

    //RETRIEVE SPECIFIC STUDENT BY ID
    public Student getStudent(Integer id){
        return studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student Id does not exist"));
    }

    //DELETE A STUDENT
    public void removeStudent(Integer id){
        studentRepository.deleteById(id);
    }

    //UPDATE STUDENT DETAILS
    public Student updateStudent(StudentRequest request){
        //CHECK IF STUDENT EXISTS


        //RETRIEVE STUDENT UPDATE DETAILS AND UPDATE THOSE CHANGED IN THE REQUEST


        //SAVE THE CHANGES TO THE DATABASE

        return null;
    }

}
