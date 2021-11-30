package org.optaplanner.examples.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dumps all threads if the test fails.
 */
public class ThreadDumpExtension implements TestWatcher {

    private static final Logger logger = LoggerFactory.getLogger(ThreadDumpExtension.class);

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        logger.error("Thread dump of a failed test ({}):{}", context.getUniqueId(), threadDump());
    }

    private static String threadDump() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        boolean lockedMonitors = threadMXBean.isObjectMonitorUsageSupported();
        boolean lockedSynchronizers = threadMXBean.isSynchronizerUsageSupported();
        StringBuffer threadDump = new StringBuffer(System.lineSeparator());
        for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(lockedMonitors, lockedSynchronizers)) {
            threadDump.append(threadInfo.toString());
        }
        return threadDump.toString();
    }
}
