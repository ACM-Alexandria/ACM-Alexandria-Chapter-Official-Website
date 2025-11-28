package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.repository.CommiteeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteService {
    private final CommiteeRepository commiteeRepository;


    CommitteService(CommiteeRepository commiteeRepository) {
        this.commiteeRepository = commiteeRepository;
    }

    public List<Committee> getAll(){
        return commiteeRepository.findAll();
    }

    public Committee getById(Long id){
        return commiteeRepository.findById(id).orElse(null);
    }


    public Committee save(Committee committee){
        return commiteeRepository.save(committee);
    }

    public void delete(Long id){
        commiteeRepository.deleteById(id);
    }


    public boolean existsByName(String name) {
     return    commiteeRepository.existsCommitteeByName(name);
    }


}
