package com.oskopek.transporteditor.event;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ResourceBundle;

/**
 * Event with a message contained. Used for sending useful tooltip contents to the UI controllers do display to the
 * user.
 */
public abstract class StringMessageEvent {

    protected final transient ResourceBundle messages;

    private final String message;

    /**
     * Constructs a new message event with the given message.
     *
     * @param messages the resource bundle used for resolving messages to their localized versions
     * @param message the message to broadcast
     */
    public StringMessageEvent(ResourceBundle messages, String message) {
        this.messages = messages;
        this.message = message;
    }

    /**
     * Get the contained message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Format the message in a way that is more suitable for the user. Use {@link #getMessage()} as a base.
     *
     * @return the formatted and transformed message
     */
    public abstract String message();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringMessageEvent)) {
            return false;
        }
        StringMessageEvent that = (StringMessageEvent) o;
        return new EqualsBuilder().append(getMessage(), that.getMessage()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getMessage()).toHashCode();
    }

    @Override
    public String toString() {
        return messages.getString("warning") + " " + message();
    }
}
