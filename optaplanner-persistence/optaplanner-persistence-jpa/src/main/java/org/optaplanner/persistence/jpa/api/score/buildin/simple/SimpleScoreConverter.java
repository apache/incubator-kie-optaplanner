package org.optaplanner.persistence.jpa.api.score.buildin.simple;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

@Converter
public class SimpleScoreConverter implements AttributeConverter<SimpleScore, String> {

    @Override
    public String convertToDatabaseColumn(SimpleScore score) {
        if (score == null) {
            return null;
        }

        return score.toString();
    }

    @Override
    public SimpleScore convertToEntityAttribute(String scoreString) {
        if (scoreString == null) {
            return null;
        }

        return SimpleScore.parseScore(scoreString);
    }
}
