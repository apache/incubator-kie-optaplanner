package org.optaplanner.persistence.jpa.api.score.buildin.bendablebigdecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;

@Converter
public class BendableBigDecimalScoreConverter implements AttributeConverter<BendableBigDecimalScore, String> {

    @Override
    public String convertToDatabaseColumn(BendableBigDecimalScore score) {
        if (score == null) {
            return null;
        }

        return score.toString();
    }

    @Override
    public BendableBigDecimalScore convertToEntityAttribute(String scoreString) {
        if (scoreString == null) {
            return null;
        }

        return BendableBigDecimalScore.parseScore(scoreString);
    }
}
