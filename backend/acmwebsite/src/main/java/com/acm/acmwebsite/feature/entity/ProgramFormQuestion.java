package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.feature.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "program_form_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramFormQuestion implements FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    @ToString.Exclude
    private Program program;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @ElementCollection
    @CollectionTable(name = "program_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    @Builder.Default
    private List<String> options = new ArrayList<>();
}
