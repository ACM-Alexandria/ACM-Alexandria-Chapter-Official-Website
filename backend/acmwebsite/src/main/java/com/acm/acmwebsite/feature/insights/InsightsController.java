package com.acm.acmwebsite.feature.insights;

import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Overview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/insights")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
public class InsightsController {

    private final InsightsService insightsService;

    /** The global overview, for a period of 7d, 30d, 90d or all. */
    @GetMapping("/overview")
    public ResponseEntity<Overview> getOverview(@RequestParam(required = false) String range) {
        return ResponseEntity.ok(insightsService.overview(InsightsRange.parse(range)));
    }

    /** One section tab under the overview. */
    @GetMapping("/{section}")
    public ResponseEntity<?> getSection(@PathVariable String section, @RequestParam(required = false) String range) {
        if (!InsightsService.SECTIONS.contains(section)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(insightsService.section(section, InsightsRange.parse(range)));
    }
}
