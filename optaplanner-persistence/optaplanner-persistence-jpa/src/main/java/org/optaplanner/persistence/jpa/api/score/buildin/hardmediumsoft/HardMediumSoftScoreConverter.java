package org.optaplanner.persistence.jpa.api.score.buildin.hardmediumsoft;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@Converter
public class HardMediumSoftScoreConverter implements AttributeConverter<HardMediumSoftScore, String> {

    @Override
    public String convertToDatabaseColumn(HardMediumSoftScore score) {
        if (score == null) {
            return null;
        }

        return score.toString();
    }

    @Override
    public HardMediumSoftScore convertToEntityAttribute(String scoreString) {
        if (scoreString == null) {
            return null;
        }

        return HardMediumSoftScore.parseScore(scoreString);
    }
}
