package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.ErrorMessageResponse;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

  private final UserService userService;

  @GetMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
  public ResponseEntity<Page<UserDTO>> searchUsers(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) Role role,
      @RequestParam(required = false) Long committeeId,
      @RequestParam(required = false) Long clubId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<UserDTO> users = userService.searchUsers(query, role, committeeId, clubId, page, size);
    return ResponseEntity.ok(users);
  }

  @PutMapping("/{id}/role")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<?> updateUserRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
    try {
      UserDTO updated = userService.updateUserRole(id, request.getRole());
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new ErrorMessageResponse(e.getMessage()));
    }
  }

  @PutMapping("/{id}/associations")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
  public ResponseEntity<?> assignAssociations(@PathVariable UUID id, @RequestBody AssignAssociationsRequest request) {
    try {
      UserDTO updated = userService.assignCommitteeAndClubs(id, request.getCommitteeId(), request.getClubId());
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new ErrorMessageResponse(e.getMessage()));
    }
  }

  @PostMapping("/{id}/assign")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
  public ResponseEntity<?> assignUser(@PathVariable UUID id, @RequestBody com.acm.acmwebsite.User_Authentication.dto.UserAssignmentDto request) {
    try {
      UserDTO updated = userService.assignUser(id, request);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new ErrorMessageResponse(e.getMessage()));
    }
  }

  @Data
  public static class UpdateRoleRequest {
    private Role role;
  }

  @Data
  public static class AssignAssociationsRequest {
    private Long committeeId;
    private Long clubId;
  }
}
