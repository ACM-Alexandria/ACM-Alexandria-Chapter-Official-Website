package com.acm.acmwebsite.feature.badges;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeResponse;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BadgeTypeService {

    private final BadgeTypeRepository typeRepository;
    private final BadgeAwardRepository awardRepository;

    @Transactional(readOnly = true)
    public List<BadgeTypeResponse> list() {
        return typeRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(BadgeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgeType get(Long id) {
        return typeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Badge type not found"));
    }

    @Transactional
    public BadgeTypeResponse create(BadgeTypeRequest request) {
        if (typeRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BadgeConflictException("A badge called " + request.name().trim() + " already exists");
        }
        BadgeType type = new BadgeType();
        apply(type, request);
        return BadgeMapper.toResponse(typeRepository.save(type));
    }

    @Transactional
    public BadgeTypeResponse update(Long id, BadgeTypeRequest request) {
        BadgeType type = get(id);
        if (typeRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new BadgeConflictException("A badge called " + request.name().trim() + " already exists");
        }
        if (type.getPeriodType() != request.periodType() && awardRepository.existsByBadgeType_Id(id)) {
            throw new BadgeConflictException("This badge has been awarded, so its period can't change");
        }
        apply(type, request);
        return BadgeMapper.toResponse(type);
    }

    // A type that has been awarded is kept for its awards' sake; deactivate it instead.
    @Transactional
    public void delete(Long id) {
        BadgeType type = get(id);
        if (awardRepository.existsByBadgeType_Id(id)) {
            throw new BadgeConflictException(type.getName() + " has been awarded and can't be deleted. Deactivate it instead.");
        }
        typeRepository.delete(type);
    }

    private void apply(BadgeType type, BadgeTypeRequest request) {
        type.setName(request.name().trim());
        type.setDescription(request.description());
        type.setPeriodType(request.periodType());
        type.setIcon(request.icon());
        type.setDefaultWinners(request.defaultWinners());
        type.setActive(request.active());
        type.setSortOrder(request.sortOrder());
    }
}
