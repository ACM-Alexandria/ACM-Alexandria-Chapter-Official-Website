package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CrudRepository<Message,Long> {
}
