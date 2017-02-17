package com.walmart.sample.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * The Venue class represents a venue, it maintains seats and available seats.
 */
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
     * Constructs a venue and initializes the seats.
     * @param seats for the entire venue.
     */
    public Venue(final List<Seat> seats) {
        this.seats = seats;
        availableSeats = new PriorityBlockingQueue<Seat>(seats.size(), new SeatPriorityComparator());
        availableSeats.addAll(seats);
    }

    /**
     * Retrieves the best available seats.
     *
     * @param seatsRequested the number of seats requested.
     * @return seats    best available seats
     * @throws VenueException when fails to retrieve requested seats
     */
    public final List<Seat> getAvailableSeats(final int seatsRequested) throws VenueException {

        // Verify at least one
        if (seatsRequested < 1) {
            throw new VenueException("Must request at least 1 seat.");
        }

        //
        List<Seat> seats = new ArrayList<Seat>();
        if (seatsRequested > availableSeats.size()) {
            throw new VenueException("Number of tickets requested exceeds available tickets.");
        }
        availableSeats.drainTo(seats, seatsRequested);
        return seats;
    }

    /**
     * Returns seats to available seat pool.
     * @param seats being returned to the available seat pool.
     */
    public void freeUnavailableSeats(final List<Seat> seats) {
        availableSeats.addAll(seats);
    }

    /**
     * Finds the number of available seats.
     * @return count of the available seats.
     */
    public int getNumberOfAvailableSeats() {
        return availableSeats.size();
    }
}
