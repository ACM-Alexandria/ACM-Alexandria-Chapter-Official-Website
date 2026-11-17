package com.acm.acmwebsite.feature.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FAQCategoryTest {

    @Test
    void values_exposesExactlyTheSupportedPublicCategories() {
        assertThat(FAQCategory.values())
                .containsExactly(
                        FAQCategory.Joining,
                        FAQCategory.Events,
                        FAQCategory.Committees,
                        FAQCategory.Club,
                        FAQCategory.Radio,
                        FAQCategory.Accounts
                );
    }

    @Test
    void valueOf_resolvesCategoryNamesUsedByTheApi() {
        assertThat(FAQCategory.valueOf("Joining")).isEqualTo(FAQCategory.Joining);
        assertThat(FAQCategory.valueOf("Accounts")).isEqualTo(FAQCategory.Accounts);
    }
}
