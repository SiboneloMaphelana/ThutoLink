package com.sbuda.sbuda.services;

import com.sbuda.sbuda.models.Program;
import com.sbuda.sbuda.repositories.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private Program program;


    public ProgramService(ProgramRepository programRepository){
        this.programRepository = programRepository;
    }

    //CREATE A PROGRAM
    public Program createProgram(Program program){
        return programRepository.save(program);
    }

    //CREATE A LIST OF PROGRAMS
    public List<Program> createPrograms(List<Program> programs){
        return programRepository.saveAll(programs);
    }

    //RETRIEVE A LIST OF PROGRAMS
    public List<Program> getPrograms(){
        return programRepository.findAll();
    }

    //RETRIEVE A SPECIFIC PROGRAM
    public Optional<Program> getProgramById(Integer id){
        return programRepository.findById(id);
    }

    //DELETE A PROGRAM
    public void deleteProgram(Integer id){
        programRepository.deleteById(id);
    }


}
