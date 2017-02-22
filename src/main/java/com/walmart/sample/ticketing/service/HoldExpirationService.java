package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.ReservationState;
import com.walmart.sample.common.SeatHold;
import com.walmart.sample.common.VenueException;
import lombok.extern.slf4j.Slf4j;

/**
 * The runnable class that handles Hold Expiration task executor.
 */
@Slf4j
public class HoldExpirationService implements Runnable {

    /**
     * SeatHold that will be cancelled upon expiration.
     */
    private SeatHold seatHold;

    /**
     * Constructs a hold expiration service.
     *
     * @param seatHold to check hold expiration.
     */
    public HoldExpirationService(final SeatHold seatHold) {
        this.seatHold = seatHold;
    }

    /**
     * Run method for scheduled task executor.
     */
    @Override
    public void run() {
        try {
            log.debug("Checking seat hold id " + seatHold.getSeatHoldId() + " for expiration.");

            // Only cancel if seat is on hold
            if (seatHold.getState() == ReservationState.HOLD) {
                log.debug("Seat hold " + seatHold.getSeatHoldId() + " Hold expired");
                seatHold.getVenue().cancelSeatHold(seatHold);
            }
        } catch (VenueException e) {
            e.printStackTrace();
        }
    }
}
