package org.optaplanner.persistence.jaxb.impl.testdata.domain;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JaxbTestdataValue extends JaxbTestdataObject {

    public JaxbTestdataValue() {
    }

    public JaxbTestdataValue(String code) {
        super(code);
    }

}
