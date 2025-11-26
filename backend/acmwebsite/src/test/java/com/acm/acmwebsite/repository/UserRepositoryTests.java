package com.acm.acmwebsite.repository;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** UserRepositoryTests */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTests {
  @Autowired UserRepository userRepository;

  @Test
  public void userRepository_saveUser_returnSavedUser() {
    User user =
        User.builder().email("testEmail@gmail.com").passwordHash("sdkfjlsdkjfkldsjf").build();
    User saveUser = userRepository.save(user);
    Assertions.assertThat(saveUser).isNotNull();
    Assertions.assertThat(saveUser.getId()).isNotNull();
		
  }
}
