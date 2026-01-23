package com.sbuda.sbuda.controllers;

import com.sbuda.sbuda.models.Program;
import com.sbuda.sbuda.services.ProgramService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService){
        this.programService = programService;
    }

    //ADD A SINGLE PROGRAM
    @PostMapping("/add-program")
    public Program addProgram(@RequestBody Program program){
        return programService.createProgram(program);
    }

    //ADD A LIST OF PROGRAMS
    @PostMapping("add-programs")
    public List<Program> addPrograms(@RequestBody List<Program> programs){
        return programService.createPrograms(programs);
    }

    //RETRIEVE A LIST OF PROGRAMS
    @GetMapping("/programs")
    public List<Program> getPrograms(){
        return programService.getPrograms();
    }

    //GET SPECIFIC PROGRAM
    @GetMapping("/program/{id}")
    public Optional<Program> getProgram(@RequestParam Integer id){
        return programService.getProgramById(id);
    }

    //DELETE A STUDENT
    @DeleteMapping("/delete-program/{id}")
    public void removeProgram(@PathVariable Integer id){
        programService.deleteProgram(id);
    }
}
