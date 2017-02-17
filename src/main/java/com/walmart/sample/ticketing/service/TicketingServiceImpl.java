package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.Customer;
import com.walmart.sample.common.Reservation;
import com.walmart.sample.common.ReservationState;
import com.walmart.sample.common.Seat;
import com.walmart.sample.common.Venue;
import com.walmart.sample.common.VenueException;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link TicketingService}.
 */
@Slf4j
public class TicketingServiceImpl implements TicketingService {

    /**
     * The scheduled executor service for hold expiration.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Find the number of seats available.
     *
     * @param venue    the venue to find the available seats.
     * @return result  number of availabe seats
     */
    @Override
    public int getNumberOfAvailableSeats(final Venue venue) {
        return venue.getNumberOfAvailableSeats();
    }

    /**
     * Finds best seats available and creates a reservation holding the seats.
     *
     * @param customer          the customer holding the seats
     * @param venue             the venue to get best available seats
     * @param numberOfSeats     the number of seats being requested
     * @return Reservation      reservation for held seats
     * @throws VenueException   when seats not available
     */
    @Override
    public Reservation holdBestAvailableSeats(final Customer customer, final Venue venue, final int numberOfSeats) throws VenueException {
        List<Seat> seats = null;
        seats = venue.getAvailableSeats(numberOfSeats);
        Reservation reservation = Reservation.builder()
            .customer(customer)
            .seats(seats)
            .reservationTimestamp(new Date())
            .venue(venue)
            .state(ReservationState.HOLD)
            .build();
        scheduler.schedule(new HoldExpirationService(reservation), HOLD_EXPIRATION_SECONDS, TimeUnit.SECONDS);

        log.debug("Scheduled HoldExpirationService for " + reservation);
        return reservation;
    }

    /**
     * Cancels a reservation currently in hold status.
     *
     * @param reservation being cancelled
     * @throws VenueException when cancelling a reservation not in hold status
     */
    @Override
    public void cancelReservation(final Reservation reservation) throws VenueException {
        if (reservation.getState() == ReservationState.HOLD) {
            reservation.getVenue().freeUnavailableSeats(reservation.getSeats());
            reservation.setState(ReservationState.CANCELLED);
        } else {
            throw new VenueException("Reservation must be in hold state to cancel");
        }
    }

    /**
     * Confirm a reservation currently in hold status.
     *
     * @param reservation  being cancelled
     * @throws VenueException when confirming a reservation not in hold status
     */
    @Override
    public void confirmReservation(final Reservation reservation) throws VenueException {
        if (reservation.getState() == ReservationState.HOLD) {
            reservation.setState(ReservationState.RESERVED);
        } else {
            throw new VenueException("Reservation must be in hold state to confirm");
        }
    }
}
