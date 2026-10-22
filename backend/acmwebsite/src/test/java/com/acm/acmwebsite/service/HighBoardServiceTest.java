package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.service.HighBoardService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    dummyUser.setProfileImageUrl("https://cdn.example.com/pres.jpg");
    dummyUser.setLinkedinUrl("https://linkedin.com/in/pres");
    return new HighBoard(1L, "President", 1, dummyUser);
  }

  @Nested
  @DisplayName("getHighBoard")
  class GetHighBoardTests {

    @Test
    @DisplayName("getHighBoard returns all high board members")
    void getHighBoardReturnsAllMembers() {
      HighBoard m1 = createDummyHighBoard();
      User vpUser = new User();
      vpUser.setId(UUID.randomUUID());
      HighBoard m2 = new HighBoard(2L, "Vice President", 2, vpUser);
      when(highBoardRepository.findAllWithUser()).thenReturn(List.of(m1, m2));

      List<HighBoard> result = highBoardService.getHighBoard();

      assertEquals(2, result.size());
      verify(highBoardRepository, times(1)).findAllWithUser();
    }
  }

  @Nested
  @DisplayName("addHighBoardMember")
  class AddHighBoardMemberTests {

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
    @DisplayName("addHighBoardMember throws when user is null")
    void addHighBoardMember_nullUser_throws() {
      HighBoard member = new HighBoard(null, "Treasurer", 3, null);

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> highBoardService.addHighBoardMember(member));
      assertEquals("User is required", ex.getMessage());
      verify(highBoardRepository, never()).save(any());
    }

    @Test
    @DisplayName("addHighBoardMember throws when role is null")
    void addHighBoardMember_nullRole_throws() {
      HighBoard member = new HighBoard(null, null, 3, new User());

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> highBoardService.addHighBoardMember(member));
      assertEquals("Role is required", ex.getMessage());
      verify(highBoardRepository, never()).save(any());
    }

    @Test
    @DisplayName("addHighBoardMember throws when role is blank")
    void addHighBoardMember_blankRole_throws() {
      HighBoard member = new HighBoard(null, "   ", 3, new User());

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> highBoardService.addHighBoardMember(member));
      assertEquals("Role is required", ex.getMessage());
      verify(highBoardRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("updateHighBoardMember")
  class UpdateHighBoardMemberTests {

    @Test
    @DisplayName("updateHighBoardMember updates fields successfully when found and existing user is present")
    void updateHighBoardMemberSuccessfully() {
      HighBoard existing = createDummyHighBoard();
      User updatedUser = new User();
      updatedUser.setId(UUID.randomUUID());
      updatedUser.setName("Updated Name");
      updatedUser.setProfileImageUrl("https://cdn.example.com/updated.jpg");
      updatedUser.setLinkedinUrl("https://linkedin.com/in/updated");
      HighBoard updatedInfo = new HighBoard(null, "New Role", 4, updatedUser);

      when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(highBoardRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

      HighBoard result = highBoardService.updateHighBoardMember(1L, updatedInfo);

      assertNotNull(result);
      assertEquals("Updated Name", result.getUser().getName());
      assertEquals("https://cdn.example.com/updated.jpg", result.getUser().getProfileImageUrl());
      assertEquals("https://linkedin.com/in/updated", result.getUser().getLinkedinUrl());
      assertEquals("New Role", result.getRole());
      assertEquals(4, result.getOrder());
      verify(highBoardRepository, times(1)).findById(1L);
      verify(highBoardRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("updateHighBoardMember with null user fields retains existing user field values")
    void updateHighBoardMember_nullUserFields_retainsExisting() {
      HighBoard existing = createDummyHighBoard();
      User updatedUser = new User();
      updatedUser.setName(null);
      updatedUser.setProfileImageUrl(null);
      updatedUser.setLinkedinUrl(null);
      HighBoard updatedInfo = new HighBoard(null, "New Role", 2, updatedUser);

      when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(highBoardRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

      HighBoard result = highBoardService.updateHighBoardMember(1L, updatedInfo);

      assertEquals("President Name", result.getUser().getName());
      assertEquals("https://cdn.example.com/pres.jpg", result.getUser().getProfileImageUrl());
      assertEquals("https://linkedin.com/in/pres", result.getUser().getLinkedinUrl());
      assertEquals("New Role", result.getRole());
      assertEquals(2, result.getOrder());
    }

    @Test
    @DisplayName("updateHighBoardMember sets user when existing member user is null")
    void updateHighBoardMember_existingUserNull_setsUser() {
      HighBoard existing = new HighBoard(1L, "President", 1, null);
      User newUser = new User();
      newUser.setName("Direct User");
      HighBoard updatedInfo = new HighBoard(null, "Vice President", 2, newUser);

      when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(highBoardRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

      HighBoard result = highBoardService.updateHighBoardMember(1L, updatedInfo);

      assertSame(newUser, result.getUser());
      assertEquals("Vice President", result.getRole());
      assertEquals(2, result.getOrder());
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
    @DisplayName("updateHighBoardMember throws when updated user is null")
    void updateHighBoardMember_nullUser_throws() {
      HighBoard existing = createDummyHighBoard();
      HighBoard updatedInfo = new HighBoard(null, "Role", 1, null);
      when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> highBoardService.updateHighBoardMember(1L, updatedInfo));
      assertEquals("User is required", ex.getMessage());
    }

    @Test
    @DisplayName("updateHighBoardMember throws when updated role is null")
    void updateHighBoardMember_nullRole_throws() {
      HighBoard existing = createDummyHighBoard();
      HighBoard updatedInfo = new HighBoard(null, null, 1, new User());
      when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> highBoardService.updateHighBoardMember(1L, updatedInfo));
      assertEquals("Role is required", ex.getMessage());
    }

    @Test
    @DisplayName("updateHighBoardMember throws when updated role is blank")
    void updateHighBoardMember_blankRole_throws() {
      HighBoard existing = createDummyHighBoard();
      HighBoard updatedInfo = new HighBoard(null, "   ", 1, new User());
      when(highBoardRepository.findById(1L)).thenReturn(Optional.of(existing));

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> highBoardService.updateHighBoardMember(1L, updatedInfo));
      assertEquals("Role is required", ex.getMessage());
    }
  }

  @Nested
  @DisplayName("deleteHighBoardMember")
  class DeleteHighBoardMemberTests {

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
      verify(highBoardRepository, never()).deleteById(any());
    }
  }
}
