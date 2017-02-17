package com.walmart.sample.common;

/**
 * The VenueException is thrown by venue methods when there is an error while processing venue processes.
 */
public class VenueException extends Exception {

    /**
     * Constructs an Venue exception with no message.
     */
    public VenueException() {
        super();
    }

    /**
     * Constructs an Venue exception.
     *
     * @param message the message for the exception.
     */
    public VenueException(final String message) {
        super(message);
    }

}
