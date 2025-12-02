package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.enums.subscriptionStatus;
import com.acm.acmwebsite.feature.repository.CommiteeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Flow;

@Service
public class CommitteService {
    private final CommiteeRepository commiteeRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    CommitteService(CommiteeRepository commiteeRepository, SubscriptionService subscriptionService , EmailService emailService) {
        this.commiteeRepository = commiteeRepository;
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
    }
    public void sendCallMessage(String topicToken, Message message) {
        var subscrption = subscriptionService.getAllSubscribersByTopicToken(topicToken);
        for (Subscription subscription : subscrption) {
            if(subscription.getStatus()== subscriptionStatus.ACTIVE)
            {
                //System.out.println("am in sending call message");
                emailService.sendEmail(subscription.getEmail(),message);
            }
        }
    }



    public List<Committee> getAll(){
        return commiteeRepository.getAll();
    }

    public Committee getById(Long id){
        return commiteeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Committee save(Committee committee){
        committee= commiteeRepository.save(committee);
        committee.initToken();
        return committee;
    }

    public void delete(Long id){
        commiteeRepository.deleteById(id);
    }


    public boolean existsByName(String name) {
     return    commiteeRepository.existsCommitteeByName(name);
    }


}
