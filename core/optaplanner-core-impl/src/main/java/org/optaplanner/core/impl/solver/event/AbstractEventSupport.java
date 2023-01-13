package org.optaplanner.core.impl.solver.event;

import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class AbstractEventSupport<E extends EventListener> {

    private final AtomicLong ID_COUNTER = new AtomicLong();

    /**
     * The requirements for this collection are as follows:
     *
     * <ul>
     * <li>Acts on value identity, not equality;</li>
     * <li>maintains insertion order.</li>
     * </ul>
     *
     * In the absence of LinkedIdentityHashSet, this has to do.
     */
    private final SortedMap<Long, E> eventListenerMap = new TreeMap<>();

    public void addEventListener(E eventListener) {
        eventListenerMap.put(ID_COUNTER.getAndIncrement(), eventListener);
    }

    public void removeEventListener(E eventListener) {
        for (Map.Entry<Long, E> entry : eventListenerMap.entrySet()) {
            if (entry.getValue() == eventListener) {
                eventListenerMap.remove(entry.getKey());
                return;
            }
        }
        throw new IllegalStateException(
                "Impossible state: event listener (" + eventListener + ") not found in map (" + eventListenerMap + ").");
    }

    protected Collection<E> getValues() {
        return eventListenerMap.values();
    }

}
