/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.persistence.jackson.api.score;

import java.io.IOException;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * Jackson binding support for a {@link Score} subtype.
 * For a {@link Score} field, use {@link PolymorphicScoreJacksonSerializer} instead,
 * so the score type is recorded too and it can be deserialized.
 * <p>
 * For example: use
 * {@code @JsonSerialize(using = HardSoftScoreJacksonSerializer.class) @JsonDeserialize(using = HardSoftScoreJacksonDeserializer.class)}
 * on a {@code HardSoftScore score} field and it will marshalled to JSON as {@code "score":"-999hard/-999soft"}.
 * Or better yet, use {@link OptaPlannerJacksonModule} instead.
 *
 * @see Score
 * @param <Score_> the actual score type
 */
public abstract class AbstractScoreJacksonSerializer<Score_ extends Score<Score_>> extends JsonSerializer<Score_>
        implements ContextualSerializer {

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
            throws JsonMappingException {
        JavaType propertyType = property.getType();
        if (Score.class.equals(propertyType.getRawClass())) {
            // If the property type is Score (not HardSoftScore for example),
            // delegate to PolymorphicScoreJacksonSerializer instead to write the score type too
            // This presumes that OptaPlannerJacksonModule is registered
            return provider.findValueSerializer(propertyType);
        }
        return this;
    }

    @Override
    public void serialize(Score_ score, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeString(score.toString());
    }

}
