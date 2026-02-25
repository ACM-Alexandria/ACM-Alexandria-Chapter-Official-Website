package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HighBoardService {

  private final HighBoardRepository highBoardRepository;

  public HighBoardService(HighBoardRepository highBoardRepository) {
    this.highBoardRepository = highBoardRepository;
  }

  public List<HighBoard> getHighBoard() {
    return highBoardRepository.findAll();
  }
}
