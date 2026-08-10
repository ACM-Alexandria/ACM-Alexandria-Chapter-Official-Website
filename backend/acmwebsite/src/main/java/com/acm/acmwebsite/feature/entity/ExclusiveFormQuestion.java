package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.feature.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exclusive_form_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExclusiveFormQuestion implements FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    @ToString.Exclude
    private ExclusiveForm exclusiveForm;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @ElementCollection
    @CollectionTable(name = "exclusive_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    @Builder.Default
    private List<String> options = new ArrayList<>();
}
