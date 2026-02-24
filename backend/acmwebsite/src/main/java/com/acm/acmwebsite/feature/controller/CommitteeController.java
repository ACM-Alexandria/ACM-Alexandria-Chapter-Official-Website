package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.SubscriptionDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.service.CommitteeService;
import com.acm.acmwebsite.feature.service.MessageService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/committee")
public class CommitteeController {
    private CommitteeService committeeService;
    private SubscriptionService subscriptionService;
    private MessageService messageService;

    public void CommitteeController(CommitteeService committeeService, SubscriptionService subscriptionService, MessageService messageService) {
        this.committeeService = committeeService;
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
    }
    @GetMapping
    List<Committee> getAllCommittees(){
        return  committeeService.getAllCommittees();
    }
    // to avoid this Reddundancy in code I suggest using mappers and add its dependencies
    @GetMapping("/{id}")
    public ResponseEntity<Committee> findCommitteeById(@PathVariable Long id){
        var committee = committeeService.getCommitteeById(id);
        if(committee == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(committee);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommittee(@PathVariable Long id,
                                             @RequestBody CommitteeDto committeeDto) {
        try {
            CommitteeDto updated = committeeService.updateCommittee(id, committeeDto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?>  createCommittee(@RequestBody CommitteeDto committeeDto){
        var newCommittee=new Committee(
                committeeDto.getName(),
                committeeDto.getDescription(),
                committeeDto.getLogoUrl(),
                committeeDto.isOpen(),
                committeeDto.getCallMessage(),
                committeeDto.getApplicationFormLink());
        try {
            committeeService.saveCommittee(newCommittee);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("message","this committee is already exist"));
        }

        return ResponseEntity.ok(new CommitteeDto(newCommittee.getId(),
                newCommittee.getName(), newCommittee.getDescription(), newCommittee.getLogoUrl()
        ,newCommittee.getCallMessage(), newCommittee.isOpen(),newCommittee.getApplicationFormLink()));

    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCommittee(@PathVariable Long id){
        var committee=committeeService.getCommitteeById(id);
        if(committee==null){
            return ResponseEntity.notFound().build();
        }
        committeeService.deleteCommittee(id);
        subscriptionService.deleteByTopic(SubscripeTo.COMMITTEE,committee.getId());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/open-call")
    public ResponseEntity<?> openCall(@PathVariable Long id){
        var committee=committeeService.getCommitteeById(id);
        if(committee==null){
            return ResponseEntity.notFound().build();
        }
        if(committee.isOpen())
        {
            return ResponseEntity.badRequest().body(Map.of("open","the call is already open!"));
        }

        committee.setOpen(true);
        committeeService.saveCommittee(committee);
        committeeService.sendCallMessage(SubscripeTo.COMMITTEE,committee.getId(),committee.getCallMessage());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/close-call")
    public ResponseEntity<?> closeCall(@PathVariable Long id){
        var committee=committeeService.getCommitteeById(id);
        if(committee==null){
            return ResponseEntity.notFound().build();
        }
        committee.setOpen(false);
        committeeService.saveCommittee(committee);
        return ResponseEntity.ok().build();
    }


    //may admin need to change message without open the calls
    @PostMapping("{id}/change-message")
    public ResponseEntity<?> changeCallMessage(@PathVariable Long id,
                                               @RequestBody Message message) {
        try {
            committeeService.changeCallMessage(id, message);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }



    @PostMapping("/{id}/subscribe")
    public ResponseEntity<?> subscribe(@PathVariable Long id,
                                       @RequestBody SubscriptionDto subscriptionDto) {
        try {
            subscriptionService.subscribeToCommittee(id, subscriptionDto);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }







}
