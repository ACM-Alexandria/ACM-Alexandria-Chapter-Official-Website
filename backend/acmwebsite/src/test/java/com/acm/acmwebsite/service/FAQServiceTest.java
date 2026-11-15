package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.FAQDto;
import com.acm.acmwebsite.feature.dto.FAQReorderDto;
import com.acm.acmwebsite.feature.entity.FAQ;
import com.acm.acmwebsite.feature.enums.FAQCategory;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.FAQRepository;
import com.acm.acmwebsite.feature.service.FAQService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FAQServiceTest {

    @Mock
    private FAQRepository faqRepository;

    @InjectMocks
    private FAQService faqService;

    private FAQ existingFaq;

    @BeforeEach
    void setUp() {
        existingFaq = faq(1, "Old question", "Old answer", FAQCategory.Joining, 0);
    }

    @Test
    void updateFAQ_updatesEveryEditableFieldAndSavesIt() {
        FAQDto update = FAQDto.builder()
                .question("  How do I register?  ")
                .answer("  Use the registration form.  ")
                .category(FAQCategory.Events)
                .order(3)
                .build();
        when(faqRepository.findById(1)).thenReturn(Optional.of(existingFaq));
        when(faqRepository.save(existingFaq)).thenReturn(existingFaq);

        FAQDto result = faqService.updateFAQ(1, update);

        assertThat(result)
                .extracting(FAQDto::getQuestion, FAQDto::getAnswer, FAQDto::getCategory, FAQDto::getOrder)
                .containsExactly("How do I register?", "Use the registration form.", FAQCategory.Events, 3);
        verify(faqRepository).save(existingFaq);
    }

    @Test
    void updateFAQ_throwsWhenTheEntryDoesNotExist() {
        when(faqRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.updateFAQ(99, dto("Question", "Answer", FAQCategory.Joining, 0)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("FAQ with id 99 does not exist.");
        verify(faqRepository, never()).save(any());
    }

    @Test
    void reorderFAQs_updatesTheRequestedOrdersAndReturnsTheSortedEntries() {
        FAQ anotherFaq = faq(2, "Second", "Answer", FAQCategory.Joining, 1);
        when(faqRepository.findById(1)).thenReturn(Optional.of(existingFaq));
        when(faqRepository.findById(2)).thenReturn(Optional.of(anotherFaq));
        when(faqRepository.findAllByOrderByCategoryAscOrderAscIdAsc()).thenReturn(List.of(anotherFaq, existingFaq));

        List<FAQDto> result = faqService.reorderFAQs(List.of(
                new FAQReorderDto(1, 1),
                new FAQReorderDto(2, 0)
        ));

        assertThat(existingFaq.getOrder()).isEqualTo(1);
        assertThat(anotherFaq.getOrder()).isZero();
        assertThat(result).extracting(FAQDto::getId).containsExactly(2, 1);
        verify(faqRepository).findById(1);
        verify(faqRepository).findById(2);
    }

    @Test
    void deleteFAQ_deletesTheResolvedEntry() {
        when(faqRepository.findById(1)).thenReturn(Optional.of(existingFaq));

        faqService.deleteFAQ(1);

        verify(faqRepository).delete(existingFaq);
    }

    @Test
    void deleteFAQ_throwsAndDoesNotDeleteWhenTheEntryDoesNotExist() {
        when(faqRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.deleteFAQ(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("FAQ with id 99 does not exist.");
        verify(faqRepository, never()).delete(any());
    }

    private FAQ faq(int id, String question, String answer, FAQCategory category, int order) {
        FAQ faq = new FAQ();
        faq.setId(id);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setCategory(category);
        faq.setOrder(order);
        return faq;
    }

    private FAQDto dto(String question, String answer, FAQCategory category, int order) {
        return FAQDto.builder().question(question).answer(answer).category(category).order(order).build();
    }
}
