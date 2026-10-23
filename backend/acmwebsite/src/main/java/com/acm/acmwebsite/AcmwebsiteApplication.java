package com.acm.acmwebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class AcmwebsiteApplication {

  // The app's business clock is Egypt time, whatever zone the server runs in.
  // LocalDateTime.now() and the other default-zone calls read this default.
  public static final String APP_TIME_ZONE = "Africa/Cairo";

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone(APP_TIME_ZONE));
    SpringApplication.run(AcmwebsiteApplication.class, args);
  }
}
