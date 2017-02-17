package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.ReservationState;
import com.walmart.sample.common.VenueException;
import com.walmart.sample.common.Reservation;
import lombok.extern.slf4j.Slf4j;

/**
 * The runnable class that handles Hold Expiration task executor.
 */
@Slf4j
public class HoldExpirationService implements Runnable {

    /**
     * Reservation that will be cancelled upon expiration.
     */
    private Reservation reservation;

    /**
     * The ticket service.
     */
    private static TicketingService ticketService = new TicketingServiceImpl();

    /**
     * Constructs a hold expiration service.
     *
     * @param reservation to check hold expiration.
     */
    public HoldExpirationService(final Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * Run method for scheduled task executor.
     */
    @Override
    public void run() {
        try {
            // if reservation is on hold cancel
            log.debug("Checking reservation for hold expiration " + reservation);
            if (reservation.getState() == ReservationState.HOLD) {
                log.debug("Hold threshold exceeded, cancelling " + reservation);
                ticketService.cancelReservation(reservation);
            }
        } catch (VenueException e) {
            e.printStackTrace();
        }
    }
}
