package com.acm.acmwebsite.feature.repository;


import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.entity.Subscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.acm.acmwebsite.feature.enums.*;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription,Long> {


    List<Subscription> getSubscriptionsBySubscribeTo( SubscripeTo subscribeTo);

    void deleteSubscriptionBySubscribeTo(SubscripeTo topic);

    void deleteSubscriptionBySubscribeToAndSubscribeToId(SubscripeTo topic, Long id);

    List<Subscription> getSubscriptionsBySubscribeToAndSubscribeToId(SubscripeTo subscripeTo, Long id);

    List<Subscription> getSubscriptionsBySubscribeToAndSubscribeToIdAndStatus(SubscripeTo subscribeTo, Long subscribeToId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.user.email = :email AND s.subscribeTo = :topic AND s.subscribeToId = :id")
    Optional<Subscription> findSubscriptionByUserEmailAndSubscribeToAndSubscribeToId(@Param("email") String email, @Param("topic") SubscripeTo topic, @Param("id") Long id);

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Subscription s")
    long countByUserId();
}
