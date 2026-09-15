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
import java.util.UUID;
import com.acm.acmwebsite.User_Authentication.entity.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HighBoardServiceTest {

  @InjectMocks
  HighBoardService highBoardService;

  @Mock
  HighBoardRepository highBoardRepository;

  private HighBoard createDummyHighBoard() {
    User dummyUser = new User();
    dummyUser.setId(UUID.randomUUID());
    dummyUser.setName("President Name");
    return new HighBoard(1L, "President", 1, dummyUser);
  }

  @Test
  @DisplayName("getHighBoard returns all high board members")
  void getHighBoardReturnsAllMembers() {
    HighBoard m1 = createDummyHighBoard();
    User vpUser = new User();
    vpUser.setId(UUID.randomUUID());
    HighBoard m2 = new HighBoard(2L, "Vice President", 2, vpUser);
    when(highBoardRepository.findAll()).thenReturn(List.of(m1, m2));

    List<HighBoard> result = highBoardService.getHighBoard();

    assertEquals(2, result.size());
    verify(highBoardRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("addHighBoardMember saves high board member successfully")
  void addHighBoardMemberSuccessfully() {
    User newUser = new User();
    newUser.setId(UUID.randomUUID());
    newUser.setName("New Name");
    HighBoard member = new HighBoard(null, "Treasurer", 3, newUser);
    HighBoard saved = new HighBoard(3L, "Treasurer", 3, newUser);
    when(highBoardRepository.save(member)).thenReturn(saved);

    HighBoard result = highBoardService.addHighBoardMember(member);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertEquals("New Name", result.getUser().getName());
    verify(highBoardRepository, times(1)).save(member);
  }

  @Test
  @DisplayName("updateHighBoardMember updates fields successfully when found")
  void updateHighBoardMemberSuccessfully() {
    HighBoard existing = createDummyHighBoard();
    User updatedUser = new User();
    updatedUser.setId(UUID.randomUUID());
    updatedUser.setName("Updated Name");
    HighBoard updatedInfo = new HighBoard(null, "New Role", 4, updatedUser);
    HighBoard saved = new HighBoard(1L, "New Role", 4, updatedUser);

    when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(highBoardRepository.save(existing)).thenReturn(saved);

    HighBoard result = highBoardService.updateHighBoardMember(1L, updatedInfo);

    assertNotNull(result);
    assertEquals("Updated Name", result.getUser().getName());
    assertEquals("New Role", result.getRole());
    verify(highBoardRepository, times(1)).findById(1L);
    verify(highBoardRepository, times(1)).save(existing);
  }

  @Test
  @DisplayName("updateHighBoardMember throws exception when member not found")
  void updateHighBoardMemberThrowsWhenNotFound() {
    User updatedUser = new User();
    HighBoard updatedInfo = new HighBoard(null, "New Role", 4, updatedUser);
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
