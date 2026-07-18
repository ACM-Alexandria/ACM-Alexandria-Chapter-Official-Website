package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.BugReport;
import com.acm.acmwebsite.feature.entity.BugReportImage;
import com.acm.acmwebsite.feature.entity.FeatureSuggestion;
import com.acm.acmwebsite.feature.repository.BugReportRepository;
import com.acm.acmwebsite.feature.repository.FeatureSuggestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.feature.dto.BugReportResponse;
import com.acm.acmwebsite.feature.dto.FeatureSuggestionResponse;
import com.acm.acmwebsite.feature.enums.FeedbackStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeatureSuggestionRepository featureSuggestionRepository;
    private final BugReportRepository bugReportRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeatureSuggestionRepository featureSuggestionRepository,
                           BugReportRepository bugReportRepository,
                           UserRepository userRepository) {
        this.featureSuggestionRepository = featureSuggestionRepository;
        this.bugReportRepository = bugReportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeatureSuggestion saveFeatureSuggestion(FeatureSuggestion fs, String userEmail) {
        if (userEmail != null && !userEmail.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                fs.setReporterId(user.getId());
            }
        }
        return featureSuggestionRepository.save(fs);
    }

    @Transactional
    public BugReport saveBugReport(BugReport br, List<String> imageUrls, String userEmail) {
        if (userEmail != null && !userEmail.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                br.setReporterId(user.getId());
            }
        }

        if (imageUrls != null) {
            List<BugReportImage> images = imageUrls.stream()
                    .map(url -> new BugReportImage(br, url))
                    .collect(Collectors.toList());
            br.setImages(images);
        }

        return bugReportRepository.save(br);
    }

    @Transactional(readOnly = true)
    public List<FeatureSuggestionResponse> getAllFeatureSuggestions() {
        return featureSuggestionRepository.findAllSorted().stream()
                .map(fs -> {
                    String rName = null;
                    String rEmail = null;
                    if (fs.getReporter() != null) {
                        rName = fs.getReporter().getName();
                        rEmail = fs.getReporter().getEmail();
                    }
                    return new FeatureSuggestionResponse(
                            fs.getId(),
                            fs.getReporterId(),
                            rName,
                            rEmail,
                            fs.getName(),
                            fs.getDescription(),
                            fs.getStatus() != null ? fs.getStatus().name() : null,
                            fs.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BugReportResponse> getAllBugReports() {
        return bugReportRepository.findAllSorted().stream()
                .map(br -> {
                    String rName = null;
                    String rEmail = null;
                    if (br.getReporter() != null) {
                        rName = br.getReporter().getName();
                        rEmail = br.getReporter().getEmail();
                    }
                    List<String> imageUrls = br.getImages().stream()
                            .map(BugReportImage::getImageUrl)
                            .collect(Collectors.toList());
                    return new BugReportResponse(
                            br.getId(),
                            br.getReporterId(),
                            rName,
                            rEmail,
                            br.getName(),
                            br.getDescription(),
                            imageUrls,
                            br.getStatus() != null ? br.getStatus().name() : null,
                            br.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void markFeatureSuggestionAsDone(Long id) {
        FeatureSuggestion fs = featureSuggestionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature suggestion not found with id: " + id));
        fs.setStatus(FeedbackStatus.DONE);
        featureSuggestionRepository.save(fs);
    }

    @Transactional
    public void markBugReportAsDone(Long id) {
        BugReport br = bugReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found with id: " + id));
        br.setStatus(FeedbackStatus.DONE);
        bugReportRepository.save(br);
    }
}
