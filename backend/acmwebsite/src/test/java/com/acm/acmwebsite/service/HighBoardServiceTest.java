package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.service.HighBoardService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HighBoardServiceTest {

  @InjectMocks
  HighBoardService highBoardService;

  @Mock
  HighBoardRepository highBoardRepository;

  private HighBoard createDummyHighBoard() {
    return new HighBoard(1L, "President Name", "president.jpg", "President", 1, "linkedin.com/in/president");
  }

  @Test
  @DisplayName("getHighBoard returns all high board members")
  void getHighBoardReturnsAllMembers() {
    HighBoard m1 = createDummyHighBoard();
    HighBoard m2 = new HighBoard(2L, "VP Name", "vp.jpg", "Vice President", 2, "linkedin.com/in/vp");
    when(highBoardRepository.findAll()).thenReturn(List.of(m1, m2));

    List<HighBoard> result = highBoardService.getHighBoard();

    assertEquals(2, result.size());
    verify(highBoardRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("addHighBoardMember saves high board member successfully")
  void addHighBoardMemberSuccessfully() {
    HighBoard member = new HighBoard(null, "New Name", "new.jpg", "Treasurer", 3, "linkedin.com/in/new");
    HighBoard saved = new HighBoard(3L, "New Name", "new.jpg", "Treasurer", 3, "linkedin.com/in/new");
    when(highBoardRepository.save(member)).thenReturn(saved);

    HighBoard result = highBoardService.addHighBoardMember(member);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertEquals("New Name", result.getName());
    verify(highBoardRepository, times(1)).save(member);
  }

  @Test
  @DisplayName("updateHighBoardMember updates fields successfully when found")
  void updateHighBoardMemberSuccessfully() {
    HighBoard existing = createDummyHighBoard();
    HighBoard updatedInfo = new HighBoard(null, "Updated Name", "updated.jpg", "New Role", 4, "linkedin.com/in/updated");
    HighBoard saved = new HighBoard(1L, "Updated Name", "updated.jpg", "New Role", 4, "linkedin.com/in/updated");

    when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(highBoardRepository.save(existing)).thenReturn(saved);

    HighBoard result = highBoardService.updateHighBoardMember(1L, updatedInfo);

    assertNotNull(result);
    assertEquals("Updated Name", result.getName());
    assertEquals("New Role", result.getRole());
    verify(highBoardRepository, times(1)).findById(1L);
    verify(highBoardRepository, times(1)).save(existing);
  }

  @Test
  @DisplayName("updateHighBoardMember throws exception when member not found")
  void updateHighBoardMemberThrowsWhenNotFound() {
    HighBoard updatedInfo = new HighBoard(null, "Updated Name", "updated.jpg", "New Role", 4, "linkedin.com/in/updated");
    when(highBoardRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> highBoardService.updateHighBoardMember(99L, updatedInfo));
  }

  @Test
  @DisplayName("deleteHighBoardMember deletes successfully when found")
  void deleteHighBoardMemberSuccessfully() {
    when(highBoardRepository.existsById(1L)).thenReturn(true);

    highBoardService.deleteHighBoardMember(1L);

    verify(highBoardRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("deleteHighBoardMember throws exception when not found")
  void deleteHighBoardMemberThrowsWhenNotFound() {
    when(highBoardRepository.existsById(99L)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> highBoardService.deleteHighBoardMember(99L));
  }
}
