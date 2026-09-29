package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HighBoardService {

  private final HighBoardRepository highBoardRepository;
  private static final Logger logger = LoggerFactory.getLogger(HighBoardService.class);
  public HighBoardService(HighBoardRepository highBoardRepository) {
    this.highBoardRepository = highBoardRepository;
  }

  @Cacheable(value = "homepageData", key = "'HighBoard'")
  public List<HighBoard> getHighBoard() {
      logger.info("Fetching HighBoards from Database...");
    return highBoardRepository.findAllWithUser();
  }
  @CacheEvict(value = "homepageData", allEntries = true)
  public HighBoard addHighBoardMember(HighBoard highBoard) {
    if (highBoard.getUser() == null) {
      throw new IllegalArgumentException("User is required");
    }
    if (highBoard.getRole() == null || highBoard.getRole().trim().isEmpty()) {
      throw new IllegalArgumentException("Role is required");
    }
    return highBoardRepository.save(highBoard);
  }

  @CacheEvict(value = "homepageData", allEntries = true)
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

  @CacheEvict(value = "homepageData", allEntries = true)
  public void deleteHighBoardMember(Long id) {
    if (!highBoardRepository.existsById(id)) {
      throw new EntityNotFoundException("High Board member not found with id " + id);
    }
    highBoardRepository.deleteById(id);
  }
}
