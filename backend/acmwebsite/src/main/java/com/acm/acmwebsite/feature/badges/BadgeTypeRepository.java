package com.acm.acmwebsite.feature.badges;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeTypeRepository extends JpaRepository<BadgeType, Long> {

    List<BadgeType> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
