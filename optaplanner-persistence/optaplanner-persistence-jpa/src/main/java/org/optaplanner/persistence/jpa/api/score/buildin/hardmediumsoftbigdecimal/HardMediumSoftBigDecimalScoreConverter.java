package org.optaplanner.persistence.jpa.api.score.buildin.hardmediumsoftbigdecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;

@Converter
public class HardMediumSoftBigDecimalScoreConverter implements AttributeConverter<HardMediumSoftBigDecimalScore, String> {

    @Override
    public String convertToDatabaseColumn(HardMediumSoftBigDecimalScore score) {
        if (score == null) {
            return null;
        }

        return score.toString();
    }

    @Override
    public HardMediumSoftBigDecimalScore convertToEntityAttribute(String scoreString) {
        if (scoreString == null) {
            return null;
        }

        return HardMediumSoftBigDecimalScore.parseScore(scoreString);
    }
}
