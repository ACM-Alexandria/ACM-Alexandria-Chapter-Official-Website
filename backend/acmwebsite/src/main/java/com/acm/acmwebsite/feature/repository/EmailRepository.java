package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.Email;
import org.springframework.data.repository.CrudRepository;

public interface EmailRepository extends CrudRepository<Email, Long> {

    boolean existsEmailByEmail(String email);

    Email getEmailByEmailId(long id);

    Email getEmailByEmail(String email);


}
