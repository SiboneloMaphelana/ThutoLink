package com.sbuda.sbuda.controllers;

import com.sbuda.sbuda.models.Program;
import com.sbuda.sbuda.services.ProgramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService){
        this.programService = programService;
    }

    @PostMapping("/add-program")
    public Program addProgram(@RequestBody Program program){
        return programService.createProgram(program);
    }

    @GetMapping("/programs")
    public List<Program> getPrograms(){
        return programService.getPrograms();
    }
}
