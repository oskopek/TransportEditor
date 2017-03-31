package com.oskopek.transport.weld;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Listener for undelivered events. Logs the event as an error.
 */
@Singleton
public final class DeadEventListener {

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Private default constructor for CDI and to prevent instantiation.
     */
    private DeadEventListener() {
        // intentionally empty
    }

    /**
     * Listen to dead events and log them.
     *
     * @param event the dead event
     */
    @Subscribe
    public void listen(DeadEvent event) {
        logger.error("Dead event ({}) pushed from ({})", event.getEvent(), event.getSource());
    }
}
