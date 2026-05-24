package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HighBoardService {

  private final HighBoardRepository highBoardRepository;

  public HighBoardService(HighBoardRepository highBoardRepository) {
    this.highBoardRepository = highBoardRepository;
  }

  public List<HighBoard> getHighBoard() {
    return highBoardRepository.findAll();
  }

  @Transactional
  public HighBoard addHighBoardMember(HighBoard highBoard) {
    if (highBoard.getName() == null || highBoard.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }
    if (highBoard.getRole() == null || highBoard.getRole().trim().isEmpty()) {
      throw new IllegalArgumentException("Role is required");
    }
    return highBoardRepository.save(highBoard);
  }

  @Transactional
  public HighBoard updateHighBoardMember(Long id, HighBoard updated) {
    return highBoardRepository.findById(id).map(member -> {
      if (updated.getName() == null || updated.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("Name is required");
      }
      if (updated.getRole() == null || updated.getRole().trim().isEmpty()) {
        throw new IllegalArgumentException("Role is required");
      }
      member.setName(updated.getName());
      member.setRole(updated.getRole());
      member.setImageUrl(updated.getImageUrl());
      member.setOrder(updated.getOrder());
      member.setLinkedinUrl(updated.getLinkedinUrl());
      return highBoardRepository.save(member);
    }).orElseThrow(() -> new EntityNotFoundException("High Board member not found with id " + id));
  }

  @Transactional
  public void deleteHighBoardMember(Long id) {
    if (!highBoardRepository.existsById(id)) {
      throw new EntityNotFoundException("High Board member not found with id " + id);
    }
    highBoardRepository.deleteById(id);
  }
}
