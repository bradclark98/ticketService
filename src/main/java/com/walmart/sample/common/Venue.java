package com.walmart.sample.common;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The Venue class represents a venue, it maintains seats and available seats.
 */
@Slf4j
public class Venue {

    /**
     * Priority queue for available seats.
     */
    private PriorityBlockingQueue<Seat> availableSeats;

    /**
     * All seats for the venue.
     */
    private List<Seat> seats;

    /**
     * Seat holds.
     */
    private Map<Integer, SeatHold> seatHoldHash;

    /**
     * Unique Id counter for seatHold.
     */
    private AtomicInteger seatHoldIdCounter;

    /**
     * Venue Hold Policy.
     */
    public static final int HOLD_EXPIRATION_SECONDS = 3;

    /**
     * Constructs a venue and initializes the seats.
     *
     * @param seats for the entire venue.
     */
    public Venue(final List<Seat> seats) {
        this.seats = seats;
        availableSeats = new PriorityBlockingQueue<Seat>(seats.size(), new SeatPriorityComparator());
        availableSeats.addAll(seats);
        seatHoldIdCounter = new AtomicInteger();
        seatHoldHash = new ConcurrentHashMap<Integer, SeatHold>();
    }

    /**
     * Find seatHolds for a venue.
     *
     * @param seatHoldId    Id of seatHold
     * @return seatHold
     */
    public SeatHold getSeatHold(final int seatHoldId) {
        return seatHoldHash.get(seatHoldId);
    }

    /**
     * Retrieves the best available seats.
     *
     * @param customerEmail the email of customer requesting seat.
     * @param seatsRequested the number of seats requested.
     * @return seats    best available seats
     * @throws VenueException when fails to retrieve requested seats
     */
    public final SeatHold getAvailableSeats(final String customerEmail, final int seatsRequested) {

        // Verify at least one seat is being requested
        if (seatsRequested < 1) {
            throw new VenueException("Must request at least 1 seat.");
        }

        List<Seat> seats = new ArrayList<Seat>();
        if (seatsRequested > availableSeats.size()) {
            throw new VenueException("Number of tickets requested exceeds available tickets.");
        }
        availableSeats.drainTo(seats, seatsRequested);

        int seatHoldId = seatHoldIdCounter.incrementAndGet();

        //TODO for simplicity made confirmation code a concatenation of hold code and email.
        SeatHold seatHold = SeatHold.builder()
            .customerEmail(customerEmail)
            .seats(seats)
            .venue(this)
            .state(ReservationState.HOLD)
            .seatHoldId(seatHoldId)
            .confirmationCode(customerEmail + seatHoldId)
            .build();

        seatHoldHash.put(seatHold.getSeatHoldId(), seatHold);

        return seatHold;
    }

    /**
     * Updates the SeatHold State Atomically.
     *
     * @param seatHoldId    The id of the SeatHold
     * @param customerEmail The customer email
     * @param state         The target state
     * @return boolean value it atomic replace occured.
     */
    public final boolean updateSeatHoldState(final int seatHoldId, final String customerEmail, final ReservationState state) {
        log.debug("Updating seat hold " + seatHoldId + " state to " + state);

        SeatHold originalSeatHold = seatHoldHash.get(seatHoldId);
        return updateSeatHoldState(originalSeatHold, state);
    }

    /**
     * Cancel SeatHold.
     *
     * @param seatHold    The seat hold being cancelled.
     * @return boolean value if seat cancelled.
     */
    public final boolean cancelSeatHold(final SeatHold seatHold) {
        log.debug("Cancelling seatHold " + seatHold.getSeatHoldId());

        boolean seatHoldCancelled = updateSeatHoldState(seatHold, ReservationState.CANCELLED);

        // updateSeatHoldState can fail when seatHold is reserved before expiration.
        if (seatHoldCancelled) {
            log.debug("Canceled seatHold " + seatHold.getSeatHoldId());
            availableSeats.addAll(seatHold.getSeats());
        }
        return seatHoldCancelled;
    }

    /**
     * Updates the SeatHold State Atomically.
     *
     * @param originalSeatHold  The id of the SeatHold
     * @param state             The target state
     * @return boolean value it atomic replace occured.
     */
    public final boolean updateSeatHoldState(final SeatHold originalSeatHold, final ReservationState state) {

        log.debug("Updating seatHold state of " + originalSeatHold.getSeatHoldId() + " from " + originalSeatHold.getState() + " to " + state);
        if (state == ReservationState.RESERVED && originalSeatHold.getState() == ReservationState.CANCELLED) {
            return false;
        } else {
            SeatHold seatHold = SeatHold.builder()
                .seatHoldId(originalSeatHold.getSeatHoldId())
                .customerEmail(originalSeatHold.getCustomerEmail())
                .confirmationCode(originalSeatHold.getConfirmationCode())
                .seats(originalSeatHold.getSeats())
                .state(state)
                .venue(originalSeatHold.getVenue()).build();

            // Concurrent Hashmap only replaces if original state is same
            if (seatHoldHash.replace(seatHold.getSeatHoldId(), originalSeatHold, seatHold)) {
                originalSeatHold.setState(state);
                log.debug("Successfully set state of " + originalSeatHold.getSeatHoldId() + " to " + state);
                return true;
            } else {
                log.debug("Failed to set state of " + originalSeatHold.getSeatHoldId() + " to " + state);
                return false;
            }
        }
    }

    /**
     * Finds the number of available seats.
     *
     * @return count of the available seats.
     */
    public int getNumberOfAvailableSeats() {
        return availableSeats.size();
    }

}
