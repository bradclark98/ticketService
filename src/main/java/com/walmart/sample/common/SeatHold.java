package com.walmart.sample.common;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * The SeatHold class maintains the registration information including customer, venue and seat information.
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(exclude = { "venue", "seats" })
public class SeatHold {

    /**
     * The seat hold id.
     */
    private int seatHoldId;

    /**
     * The confirmation Code.
     */
    private String confirmationCode;

    /**
     * The state of the seatHold.
     */
    private ReservationState state;

    /**
     * The customer making the reservation.
     */
    private String customerEmail;

    /**
     * The venue.
     */
    private Venue venue;

    /**
     * List of seats in reservation.
     */
    private List<Seat> seats;
}
