package com.acm.acmwebsite.feature.repository;


import com.acm.acmwebsite.feature.entity.Subscription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription,Long> {

    List<Subscription> getSubscriptionsByTopic(String topic);
    boolean existsByEmailAndTopic(String email,String topic);
}
