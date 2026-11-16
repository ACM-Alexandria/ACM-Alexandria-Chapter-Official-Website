package com.acm.acmwebsite.feature.badges;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeTypeRepository extends JpaRepository<BadgeType, Long> {

    List<BadgeType> findAllByOrderBySortOrderAscIdAsc();

    Optional<BadgeType> findByName(String name);
}
