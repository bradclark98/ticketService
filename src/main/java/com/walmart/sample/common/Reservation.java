package com.walmart.sample.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * The Reservatioin class maintains the registration information including customer, venue and seat information.
 */
@Getter
@Setter
@Builder
@ToString
public class Reservation {

    /**
     * The time the reservation was made.
     */
    private Date reservationTimestamp;

    /**
     * The state of the reservation.
     */
    private ReservationState state;

    /**
     * The customer making the reservation.
     */
    private Customer customer;

    /**
     * The venue.
     */
    private Venue venue;

    /**
     * List of seats in reservation.
     */
    private List<Seat> seats;
}
