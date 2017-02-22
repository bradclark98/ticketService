package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.ReservationState;
import com.walmart.sample.common.SeatHold;
import com.walmart.sample.common.Venue;
import com.walmart.sample.common.VenueException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link TicketService}.
 */
@Slf4j
@Getter
@Setter
public class TicketServiceImpl implements TicketService {

    /**
     * The Venue for the ticket service.
     */
    private Venue venue;

    /**
     * The scheduled executor service for hold expiration.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Creates a TicketServiceImpl.
     *
     * @param venue for ticket service
     */
    public TicketServiceImpl(Venue venue) {
        this.venue = venue;
    }

    /**
     * Find the number of seats available.
     *
     * @return number of availabe seats
     */
    @Override
    public int numSeatsAvailable() {
        return venue.getNumberOfAvailableSeats();
    }


    /**
     * Find and hold the best available seats for a customer.
     *
     * @param numSeats the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related information
     */
    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        log.debug("FindAndHoldSeats " + numSeats + " for " + customerEmail);
        SeatHold seatHold = venue.getAvailableSeats(customerEmail, numSeats);
        log.debug("SeatHold " + seatHold.getSeatHoldId() + " created");
        scheduler.schedule(new HoldExpirationService(seatHold), Venue.HOLD_EXPIRATION_SECONDS, TimeUnit.SECONDS);
        log.debug("Scheduled HoldExpirationService for seatHold " + seatHold.getSeatHoldId());
        return seatHold;
    }

    /**
     * Commit seats held for a specific customer.
     *
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold is assigned
     * @return a reservation confirmation code
     */
    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        log.debug("reserveSeats for seatHold " + seatHoldId + " for " + customerEmail);
        SeatHold seatHold = venue.getSeatHold(seatHoldId);
        if (!venue.updateSeatHoldState(seatHold, ReservationState.RESERVED)) {
            log.debug("Unable to reserve seats, hold expired");
            throw new VenueException("Unable to reserve seats, hold expired");
        } else {
            return seatHold.getConfirmationCode();
        }
    }
}
