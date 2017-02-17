package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.VenueException;
import com.walmart.sample.common.Reservation;
import com.walmart.sample.common.Venue;
import com.walmart.sample.common.Customer;

/**
 * TicketingService for processing Ticket Requests for events.
 */
public interface TicketingService {


    /**
     * Hold expiration threshold in seconds.
     */
    public static final long HOLD_EXPIRATION_SECONDS = 6;

    /**
     * Find the number of seats available.
     *
     * @param venue    the event to available seat count
     * @return result  number of availabe seats
     */
    int getNumberOfAvailableSeats(Venue venue);

    /**
     * Finds best seats available and creates a reservation holding the seats.
     *
     * @param customer      the customer holding the seats
     * @param venue         the venue to get best available seats
     * @param numberOfSeats the number of seats being requested
     * @return Reservation  reservation for held seats
     * @throws VenueException when no available seats
     */
    Reservation holdBestAvailableSeats(Customer customer, Venue venue, int numberOfSeats) throws VenueException;

    /**
     * Cancels a reservation currently in hold status.
     *
     * @param reservation  being cancelled
     * @throws VenueException when cancelling a reservation not in hold status
     */
    void cancelReservation(Reservation reservation) throws VenueException;

    /**
     * Confirm a reservation currently in hold status.
     *
     * @param reservation  being cancelled
     * @throws VenueException when confirming a reservation not in hold status
     */
    void confirmReservation(Reservation reservation) throws VenueException;
}
