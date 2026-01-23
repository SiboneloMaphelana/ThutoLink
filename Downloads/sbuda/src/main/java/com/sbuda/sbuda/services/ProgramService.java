package com.sbuda.sbuda.services;

import com.sbuda.sbuda.models.Program;
import com.sbuda.sbuda.repositories.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private Program program;


    public ProgramService(ProgramRepository programRepository){
        this.programRepository = programRepository;
    }

    public Program createProgram(Program program){
        return programRepository.save(program);
    }

    public List<Program> getPrograms(){
        return programRepository.findAll();
    }


}
