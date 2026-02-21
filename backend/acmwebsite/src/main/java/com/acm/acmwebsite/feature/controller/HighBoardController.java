package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.service.HighBoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/highboard")
public class HighBoardController {

  private final HighBoardService highBoardService;

  public HighBoardController(HighBoardService highBoardService) {
    this.highBoardService = highBoardService;
  }

  @GetMapping
  public List<HighBoard> getHighBoard() {
    return highBoardService.getHighBoard();
  }
}
