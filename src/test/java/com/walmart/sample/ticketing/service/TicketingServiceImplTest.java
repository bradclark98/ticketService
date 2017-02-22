package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.ReservationState;
import com.walmart.sample.common.Seat;
import com.walmart.sample.common.SeatHold;
import com.walmart.sample.common.Venue;
import com.walmart.sample.common.VenueException;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Unit test for TicketServiceImpl
 */
@Slf4j
public class TicketingServiceImplTest {

    /**
     * Initial seats used in venue creation.
     */
    List<Seat> seats = new ArrayList<Seat>();

    /**
     * Rows in venue.
     */
    static final int ROWS = 10;

    /**
     * Seats in each row at venue.
     */
    static final int SEATS_PER_ROW = 10;

    /**
     * Test email.
     */
    static final String TEST_EMAIL = "email@test.com";

    /**
     * TicketingService being tested.
     */
    private TicketService ticketService;

    /**
     * Create a simple seat list for venue creation in test cases.
     */
    @BeforeClass
    public void setUp() {
        IntStream.range(1, SEATS_PER_ROW).forEach(seatNumber -> {
            IntStream.range(1, ROWS).forEach(row -> {
                //TODO Simplistic seat quality algorithm with row and seat number.  Only impacts Comparator if changed.
                seats.add(Seat.builder().rowNumber(row).seatNumber(seatNumber).seatQuality(
                    1000000 - (ROWS - row * 1000 + SEATS_PER_ROW - seatNumber)).build());
            });
        });
    }

    /**
     * Tests getNumberOfAvailableSeats method in {@code TicketServiceImpl} and the cancellation after threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeats() {

        // Reset the venue
        ticketService = new TicketServiceImpl(new Venue(seats));

        int initialAvailableSeats = ticketService.numSeatsAvailable();
        Assert.assertEquals(seats.size(), initialAvailableSeats);

        List<SeatHold> seatHolds = new ArrayList<SeatHold>();

        IntStream.range(0, 10).forEach(i -> {
            seatHolds.add(ticketService.findAndHoldSeats(5, TEST_EMAIL));
        });

        int numSeatsOnHold = 0;
        for (SeatHold seatHold : seatHolds) {
            numSeatsOnHold += seatHold.getSeats().size();
        }
        Assert.assertEquals(initialAvailableSeats - numSeatsOnHold, ticketService.numSeatsAvailable(), "Available seats should be initial seats minus held seats");

        // Wait until hold expires
        try {
            Thread.sleep((Venue.HOLD_EXPIRATION_SECONDS + 5) * 1000);
        } catch (InterruptedException e) {
            Assert.fail("Failed to sleep beyond HOLD_EXPIRATION_SECONDS");
        }

        // All seats should be available
        Assert.assertEquals(initialAvailableSeats, ticketService.numSeatsAvailable(), "All seats should be available");
    }



    /**
     * Tests getNumberOfAvailableSeats and confirmReservation method in {@code TicketServiceImpl} before cancel threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeatsAfterConfirm() {

        // Reset the venue
        ticketService = new TicketServiceImpl(new Venue(seats));

        List<SeatHold> seatHolds = new ArrayList<SeatHold>();

        try {

            int initialSeatCount = ticketService.numSeatsAvailable();

            IntStream.range(0, 10).forEach(i -> {
                seatHolds.add(ticketService.findAndHoldSeats(5, TEST_EMAIL));
            });

            int remainingSeatCount = ticketService.numSeatsAvailable();
            int holdSeatCount = 0;

            // Reserve seats
            seatHolds.forEach(seatHold -> ticketService.reserveSeats(seatHold.getSeatHoldId(), TEST_EMAIL));

            // Wait until hold expires
            try {
                Thread.sleep((Venue.HOLD_EXPIRATION_SECONDS + 5) * 1000);
            } catch (InterruptedException e) {
                Assert.fail("Failed to sleep beyond HOLD_EXPIRATION_SECONDS");
            }

            // Check for seatHolds in reserved state
            seatHolds.forEach(seatHold -> Assert.assertEquals(seatHold.getState(), ReservationState.RESERVED));

            // Verify no seatHolds were cancelled
            Assert.assertEquals(remainingSeatCount, ticketService.numSeatsAvailable());

        } catch (VenueException e) {
            Assert.fail("Unexpected VenueException thrown");
        }
    }

    /**
     * Tests the holdBestAvailableSeats method in {@code TicketServiceImpl}.
     */
    @Test(groups = {"fast", "unit"})
    public void testHoldBestAvailableSeats() {

        // Reset the venue
        ticketService = new TicketServiceImpl(new Venue(seats));

        List<SeatHold> seatHolds = new ArrayList<SeatHold>();

        int initialSeatCount = ticketService.numSeatsAvailable();
        IntStream.range(0, 10).forEach(i -> {
            try {
                seatHolds.add(ticketService.findAndHoldSeats(5, TEST_EMAIL));
            } catch (VenueException e) {
                Assert.fail("Unexpected VenueException thrown");
            }
        });

        // Check for seatHolds in hold state
        int holdSeatCount = seatHolds.stream()
            .filter(seatHold -> seatHold.getState() == ReservationState.HOLD)
            .mapToInt(seatHold -> seatHold.getSeats().size()).reduce(0, Integer::sum);

        Assert.assertEquals(ticketService.numSeatsAvailable(), initialSeatCount - holdSeatCount);
    }


    /**
     * Tests testHoldBestAvailableSeats and cancelReservation method in {@code TicketServiceImpl}, initial hold and then cancellation after threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testHoldSeatAndHoldExpiration() {

        // Reset the venue
        ticketService = new TicketServiceImpl(new Venue(seats));

        // Keep track of multiple seatHolds
        List<SeatHold> seatHolds = new ArrayList<SeatHold>();

        // Get initial seat count
        int initialSeatCount = ticketService.numSeatsAvailable();

        IntStream.range(0, 10).forEach(i -> {
            seatHolds.add(ticketService.findAndHoldSeats(5, TEST_EMAIL));
        });

        // Check for seatHolds in hold state
        for (SeatHold seatHold : seatHolds) {
            Assert.assertEquals(seatHold.getState(), ReservationState.HOLD);
        }

        // Wait until hold expires
        try {
            Thread.sleep((Venue.HOLD_EXPIRATION_SECONDS + 5) * 1000);
        } catch (InterruptedException e) {
            Assert.fail("Failed to sleep beyond HOLD_EXPIRATION_SECONDS");
        }

        // Verify SeatHold were cancelled
        for (SeatHold seatHold : seatHolds) {
            Assert.assertEquals(seatHold.getState(), ReservationState.CANCELLED);
        }

        // Verify seats were returned to available seats
        Assert.assertEquals(ticketService.numSeatsAvailable(), initialSeatCount);
    }

}
