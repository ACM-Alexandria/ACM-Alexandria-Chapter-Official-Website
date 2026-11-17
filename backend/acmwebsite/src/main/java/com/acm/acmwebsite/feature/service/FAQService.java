package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.FAQDto;
import com.acm.acmwebsite.feature.dto.FAQReorderDto;
import com.acm.acmwebsite.feature.entity.FAQ;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.FAQRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class FAQService {

    private final FAQRepository faqRepository;

    public FAQService(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }
    public FAQDto addFAQ(FAQDto faqDto) {
        FAQ faq = new FAQ();
        copyFields(faqDto, faq);
        return toDto(faqRepository.save(faq));
    }

    public Page<FAQDto> getFAQs(String query, Pageable pageable) {
        Page<FAQ> faqs = query == null || query.isBlank()
                ? faqRepository.findAll(pageable)
                : faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(query.trim(), query.trim(), pageable);
        return faqs.map(this::toDto);
    }

    public FAQDto getFAQ(int id) {
        return toDto(findFAQ(id));
    }

    public FAQDto updateFAQ(int id, FAQDto faqDto) {
        FAQ faq = findFAQ(id);
        copyFields(faqDto, faq);
        return toDto(faqRepository.save(faq));
    }

    public void deleteFAQ(int id) {
        faqRepository.delete(findFAQ(id));
    }

    @Transactional
    public List<FAQDto> reorderFAQs(List<FAQReorderDto> items) {
        for (FAQReorderDto item : items) {
            findFAQ(item.id()).setOrder(item.order());
        }
        return faqRepository.findAllByOrderByCategoryAscOrderAscIdAsc().stream().map(this::toDto).toList();
    }

    private FAQ findFAQ(int id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ with id " + id + " does not exist."));
    }

    private void copyFields(FAQDto source, FAQ target) {
        target.setQuestion(source.getQuestion().trim());
        target.setAnswer(source.getAnswer().trim());
        target.setCategory(source.getCategory());
        target.setOrder(source.getOrder());
    }

    private FAQDto toDto(FAQ faq) {
        return FAQDto.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .order(faq.getOrder())
                .build();
    }
}
