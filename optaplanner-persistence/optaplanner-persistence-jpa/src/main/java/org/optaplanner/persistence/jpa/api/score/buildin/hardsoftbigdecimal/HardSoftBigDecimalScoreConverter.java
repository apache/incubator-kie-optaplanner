package org.optaplanner.persistence.jpa.api.score.buildin.hardsoftbigdecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;

@Converter
public class HardSoftBigDecimalScoreConverter implements AttributeConverter<HardSoftBigDecimalScore, String> {

    @Override
    public String convertToDatabaseColumn(HardSoftBigDecimalScore score) {
        if (score == null) {
            return null;
        }

        return score.toString();
    }

    @Override
    public HardSoftBigDecimalScore convertToEntityAttribute(String scoreString) {
        if (scoreString == null) {
            return null;
        }

        return HardSoftBigDecimalScore.parseScore(scoreString);
    }
}
