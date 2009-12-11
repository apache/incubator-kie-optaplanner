package org.drools.planner.examples.common.app;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class LoggingMain {

    public static final String DEFAULT_LOGGING_CONFIG = "/org/drools/planner/examples/common/app/log4j.xml";

    protected final transient Logger logger;

    public LoggingMain() {
        this(DEFAULT_LOGGING_CONFIG);
    }

    public LoggingMain(String loggingConfig) {
        DOMConfigurator.configure(getClass().getResource(loggingConfig));
        logger = LoggerFactory.getLogger(getClass());
    }
    
}
