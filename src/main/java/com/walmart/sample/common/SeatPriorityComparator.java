package com.walmart.sample.common;

import java.util.Comparator;

/**
 * Compares seat quality, row and seat number to ensure best seats are given priority in PriorityQueue.
 */
public class SeatPriorityComparator implements Comparator<Seat> {

    /**
     * Compares seat quality, row and seat number to ensure best seats are given priority in PriorityQueue.
     * @param seat1 the first seat being compared.
     * @param seat2 the second seat being compared.
     * @return integer value representing the comparison of seats.
     */
    @Override
    public final int compare(final Seat seat1, final Seat seat2) {

        // Additional ordering by row and seat number ensures multiple seats are together.
        if (seat1.getSeatQuality() != seat2.getSeatQuality()) {
            // Higher quality is better
            return Integer.compare(seat2.getSeatQuality(), seat1.getSeatQuality());
        } else if (seat1.getRowNumber() != seat2.getRowNumber()) {
            // Lower row is better
            return Integer.compare(seat1.getRowNumber(), seat2.getRowNumber());
        } else {
            // Lower seat is better
            return Integer.compare(seat1.getSeatNumber(), seat2.getSeatNumber());
        }
    }

}
