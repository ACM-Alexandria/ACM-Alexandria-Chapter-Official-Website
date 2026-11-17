package com.acm.acmwebsite.repository;

import com.acm.acmwebsite.feature.entity.FAQ;
import com.acm.acmwebsite.feature.enums.FAQCategory;
import com.acm.acmwebsite.feature.repository.FAQRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FAQRepositoryTest {

    @Autowired
    private FAQRepository faqRepository;

    @Test
    void findAllByOrderByCategoryAscOrderAscIdAsc_sortsByCategoryThenOrderThenId() {
        FAQ firstEvent = save("First event", "Answer", FAQCategory.Events, 1);
        FAQ secondEvent = save("Second event", "Answer", FAQCategory.Events, 2);
        FAQ joining = save("Joining question", "Answer", FAQCategory.Joining, 0);

        List<FAQ> result = faqRepository.findAllByOrderByCategoryAscOrderAscIdAsc();

        // Enum values are stored as strings, so alphabetical category order is Events then Joining.
        assertThat(result).extracting(FAQ::getId)
                .containsExactly(firstEvent.getId(), secondEvent.getId(), joining.getId());
    }

    @Test
    void findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase_searchesBothFieldsAndPaginates() {
        FAQ questionMatch = save("How do I join ACM?", "Complete the registration form.", FAQCategory.Joining, 0);
        FAQ answerMatch = save("Event registration", "Join us at the opening session.", FAQCategory.Events, 0);
        save("Radio schedule", "Episodes are published weekly.", FAQCategory.Radio, 0);

        Page<FAQ> firstPage = faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(
                "JOIN", "JOIN", PageRequest.of(0, 1, Sort.by("id").ascending()));
        Page<FAQ> secondPage = faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(
                "join", "join", PageRequest.of(1, 1, Sort.by("id").ascending()));

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).extracting(FAQ::getId).containsExactly(questionMatch.getId());
        assertThat(secondPage.getContent()).extracting(FAQ::getId).containsExactly(answerMatch.getId());
    }

    private FAQ save(String question, String answer, FAQCategory category, int order) {
        FAQ faq = new FAQ();
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setCategory(category);
        faq.setOrder(order);
        return faqRepository.save(faq);
    }
}
