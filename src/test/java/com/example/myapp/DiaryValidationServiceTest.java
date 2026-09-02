package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiaryValidationServiceTest {

    private DiaryValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new DiaryValidationService(new InputValidationService());
    }

    @Test
    void acceptsGratitudeExpression() {
        assertThat(validationService.containsGratitude("今日も助けてくれてありがとう。"))
                .isTrue();
    }

    @Test
    void rejectsTextWithoutGratitudeExpression() {
        assertThat(validationService.containsGratitude("これは通常の文章です。"))
                .isFalse();
    }

    @Test
    void rejectsNegatedGratitudeExpression() {
        assertThat(validationService.containsGratitude("これは感謝表現を含まない異常入力です。"))
                .isFalse();
    }

    @Test
    void rejectsDifferentNegatedExpressions() {
        assertThat(validationService.containsGratitude("誰にも感謝していません。"))
                .isFalse();
        assertThat(validationService.containsGratitude("ありがとうとは思わない。"))
                .isFalse();
        assertThat(validationService.containsGratitude("お礼を言わずに帰った。"))
                .isFalse();
    }

    @Test
    void acceptsPositiveExpressionEvenWhenAnotherExpressionIsNegated() {
        assertThat(validationService.containsGratitude(
                "感謝していないわけではなく、助けてもらって本当にありがとう。"))
                .isTrue();
    }

    @Test
    void normalizesFullWidthLatinCharacters() {
        assertThat(validationService.containsGratitude("今日は３Ｋ！"))
                .isTrue();
    }

    @Test
    void acceptsEnglishGratitudeExpressions() {
        assertThat(validationService.containsGratitude("Thank you for your kindness."))
                .isTrue();
        assertThat(validationService.containsGratitude("A friend helped me today."))
                .isTrue();
        assertThat(validationService.containsGratitude("I’m grateful for the quiet morning."))
                .isTrue();
    }

    @Test
    void rejectsNegatedEnglishGratitudeExpressions() {
        assertThat(validationService.containsGratitude("I am not grateful for that."))
                .isFalse();
        assertThat(validationService.containsGratitude("I do not appreciate this."))
                .isFalse();
        assertThat(validationService.containsGratitude("Nobody helped me today."))
                .isFalse();
    }

    @Test
    void rejectsEnglishKeywordsEmbeddedInOtherWords() {
        assertThat(validationService.containsGratitude(
                "I felt ungrateful during Thanksgiving."))
                .isFalse();
        assertThat(validationService.containsGratitude("The task remained unhelped."))
                .isFalse();
    }
}
