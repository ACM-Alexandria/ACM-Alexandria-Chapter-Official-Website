package com.acm.acmwebsite.feature.badges;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeResponse;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

// The badge types are fixed (see BadgeTypeSeeder), so they are only read here.
@Service
@RequiredArgsConstructor
public class BadgeTypeService {

    private final BadgeTypeRepository typeRepository;

    @Transactional(readOnly = true)
    public List<BadgeTypeResponse> list() {
        return typeRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(BadgeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgeType get(Long id) {
        return typeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Badge type not found"));
    }
}
