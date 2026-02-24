package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Email;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmailRepository extends CrudRepository<Email, Long> {

    boolean existsEmailByEmail(String email);

    Email getEmailByEmailId(long id);

    Optional<Email> getEmailByEmail(String email);

}
