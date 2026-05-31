package com.acm.acmwebsite.feature.repository;


import com.acm.acmwebsite.feature.entity.Email;
import com.acm.acmwebsite.feature.entity.Subscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.acm.acmwebsite.feature.enums.*;
import java.util.List;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription,Long> {


    List<Subscription> getSubscriptionsBySubscribeTo( SubscripeTo subscribeTo);

    void deleteSubscriptionBySubscribeTo(SubscripeTo topic);

    void deleteSubscriptionBySubscribeToAndSubscribeToId(SubscripeTo topic, Long id);

    List<Subscription> getSubscriptionsBySubscribeToAndSubscribeToId(SubscripeTo subscripeTo, Long id);

    @Query("SELECT S FROM  Subscription  S LEFT JOIN S.email E WHERE S.id= E.emailId AND E.email=:email AND S.subscribeTo=:topic AND S.subscribeToId=:id")
    Subscription findSubscriptionByEmailAndSubscribeToAndSubscribeToId(String email, SubscripeTo topic, Long id);

    @Query("SELECT DISTINCT S.email FROM Subscription S WHERE S.status = :status AND S.email IS NOT NULL")
    List<Email> findDistinctEmailsByStatus(@Param("status") SubscriptionStatus status);
}
