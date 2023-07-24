@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(value = PolymorphicScoreJaxbAdapter.class, type = Score.class),
        @XmlJavaTypeAdapter(value = JaxbOffsetDateTimeAdapter.class, type = OffsetDateTime.class)
})
package org.optaplanner.benchmark.impl.result;

import java.time.OffsetDateTime;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.io.jaxb.adapter.JaxbOffsetDateTimeAdapter;
import org.optaplanner.persistence.jaxb.api.score.PolymorphicScoreJaxbAdapter;
