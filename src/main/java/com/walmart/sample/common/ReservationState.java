package com.walmart.sample.common;

/**
 * An enumeration of the states that a reservation can have.
 */
public enum ReservationState {

    /**
     * The initial state a Reservation is in prior to reserving.
     */
    HOLD,

    /**
     * The final state a Reservation is in after customer has confirmed.
     */
    RESERVED,

    /**
     * The state a reservation is in after a customer has cancelled or hold has expired.
     */
    CANCELLED
}
