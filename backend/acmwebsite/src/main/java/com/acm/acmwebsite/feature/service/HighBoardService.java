package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HighBoardService {

  private final HighBoardRepository highBoardRepository;

  public HighBoardService(HighBoardRepository highBoardRepository) {
    this.highBoardRepository = highBoardRepository;
  }

  public List<HighBoard> getHighBoard() {
    return highBoardRepository.findAll();
  }

  public HighBoard addHighBoardMember(HighBoard highBoard) {
    if (highBoard.getUser() == null) {
      throw new IllegalArgumentException("User is required");
    }
    if (highBoard.getRole() == null || highBoard.getRole().trim().isEmpty()) {
      throw new IllegalArgumentException("Role is required");
    }
    return highBoardRepository.save(highBoard);
  }

  public HighBoard updateHighBoardMember(Long id, HighBoard updated) {
    return highBoardRepository.findById(id).map(member -> {
      if (updated.getUser() == null) {
        throw new IllegalArgumentException("User is required");
      }
      if (updated.getRole() == null || updated.getRole().trim().isEmpty()) {
        throw new IllegalArgumentException("Role is required");
      }
      member.setUser(updated.getUser());
      member.setRole(updated.getRole());
      member.setOrder(updated.getOrder());
      return highBoardRepository.save(member);
    }).orElseThrow(() -> new EntityNotFoundException("High Board member not found with id " + id));
  }

  public void deleteHighBoardMember(Long id) {
    if (!highBoardRepository.existsById(id)) {
      throw new EntityNotFoundException("High Board member not found with id " + id);
    }
    highBoardRepository.deleteById(id);
  }
}
