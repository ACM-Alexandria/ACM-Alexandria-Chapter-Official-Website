package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.service.HighBoardService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

  @PostMapping("/members")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> addHighBoardMember(@RequestBody HighBoard highBoard) {
    try {
      HighBoard created = highBoardService.addHighBoardMember(highBoard);
      return ResponseEntity.ok(created);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/members/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> updateHighBoardMember(@PathVariable Long id, @RequestBody HighBoard highBoard) {
    try {
      HighBoard updated = highBoardService.updateHighBoardMember(id, highBoard);
      return ResponseEntity.ok(updated);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @DeleteMapping("/members/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> deleteHighBoardMember(@PathVariable Long id) {
    try {
      highBoardService.deleteHighBoardMember(id);
      return ResponseEntity.ok().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }
}
