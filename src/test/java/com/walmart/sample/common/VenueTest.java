package com.walmart.sample.common;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Unit test for Venue.
 */
@Slf4j
public class VenueTest {

    /**
     * Seats used in all tests in VenueTest.
     */
    private List<Seat> seats = new ArrayList<Seat>();

    /**
     * Rows in test venue.
     */
    static final int ROWS = 10;

    /**
     * Seats in each row of test venue.
     */
    static final int SEATS_PER_ROW = 10;

    /**
     * Test email
     */
    static final String TEST_EMAIL = "email@test.com";

    /**
     * Initialize venue seats for use in tests.
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
     * Tests the getAvailableSeats method in {@code Venue}.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetAvailableSeats() {
        Venue venue = new Venue(seats);

        int initialAvailable = venue.getNumberOfAvailableSeats();
        // Make multiple seat requests
        List<Seat> allSeatsRequested = new ArrayList<Seat>();

        IntStream.range(1, 5).forEach(i -> {
            try {
                int requestedSeats = 4;
                SeatHold seatHold = venue.getAvailableSeats(TEST_EMAIL, requestedSeats);
                Assert.assertEquals(seatHold.getSeats().size(), requestedSeats);
                allSeatsRequested.addAll(seatHold.getSeats());
            } catch (VenueException e) {
                Assert.fail("Encountered error " + e.getMessage());
            }
        });

        Assert.assertEquals(initialAvailable - allSeatsRequested.size(), venue.getNumberOfAvailableSeats());

        // Verify priorityOrder
        Seat previousSeat = null;
        for (Seat seat : allSeatsRequested) {
            if (previousSeat != null) {
                Assert.assertTrue(seat.getSeatQuality() < previousSeat.getSeatQuality()
                        || (seat.getSeatQuality() == previousSeat.getSeatQuality()
                            && seat.getRowNumber() > previousSeat.getRowNumber())
                        || (seat.getSeatQuality() == previousSeat.getSeatQuality()
                            && seat.getRowNumber() == previousSeat.getRowNumber()
                            && seat.getSeatNumber() > previousSeat.getSeatNumber()));
            }
            previousSeat = seat;
        }
    }

    /**
     * Tests getAvailableSeats method in {@code Venue} when no available seats.
     */
    @Test(groups = {"fast", "unit"})
    public void testNoAvailableSeats() {
        Venue venue = new Venue(seats);
        try {
            venue.getAvailableSeats(TEST_EMAIL, seats.size());
        } catch (VenueException e) {
            Assert.fail("Initial seat request should succeed.");
        }

        try {
            venue.getAvailableSeats(TEST_EMAIL, 1);
            Assert.fail("Seat request should fail.");
        } catch (VenueException e) {
            Assert.assertEquals(e.getMessage(), "Number of tickets requested exceeds available tickets.");
        }
    }

    /**
     * Tests getAvailableSeats method in {@code Venue} when available seats is less than requested.
     */
    @Test(groups = {"fast", "unit"})
    public void testAvailableSeatsLessThanRequested() {
        Venue venue = new Venue(seats);
        try {
            venue.getAvailableSeats(TEST_EMAIL, seats.size() - 1);
        } catch (VenueException e) {
            Assert.fail("Initial seat request should succeed.");
        }

        try {
            venue.getAvailableSeats(TEST_EMAIL, 2);
            Assert.fail("Seat request should fail.");
        } catch (VenueException e) {
            Assert.assertEquals(e.getMessage(), "Number of tickets requested exceeds available tickets.");
        }
    }

    /**
     * Tests getAvailableSeats method in {@code Venue} when request is 0.
     */
    @Test(groups = {"fast", "unit"})
    public void testAvailableSeatsWhenRequestedZero() {
        Venue venue = new Venue(seats);
        try {
            SeatHold seatHold = venue.getAvailableSeats(TEST_EMAIL, 0);
            Assert.fail("Seat request of 0 should fail.");
        } catch (VenueException e) {
            Assert.assertEquals(e.getMessage(), "Must request at least 1 seat.");
        }
    }

    /**
     * Tests getNumberOfAvailableSeats method in {@code Venue}.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeats() {
        Venue venue = new Venue(seats);
        int initialSize = venue.getNumberOfAvailableSeats();
        try {
            SeatHold seatHold = venue.getAvailableSeats(TEST_EMAIL, 1);
            Assert.assertEquals(seatHold.getSeats().size() + venue.getNumberOfAvailableSeats(), initialSize);
        } catch (VenueException e) {
            Assert.fail("Encountered error " + e.getMessage());
        }
    }

    /**
     * Tests getNumberOfAvailableSeats method in {@code Venue} when seats are cancelled.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeatsAfterCancel() {
        Venue venue = new Venue(seats);
        int initialSize = venue.getNumberOfAvailableSeats();
        try {
            SeatHold seatHold = venue.getAvailableSeats(TEST_EMAIL,1);
            Assert.assertEquals(seatHold.getSeats().size() + venue.getNumberOfAvailableSeats(), initialSize);
            venue.cancelSeatHold(seatHold);
            Assert.assertEquals(venue.getNumberOfAvailableSeats(), initialSize);
        } catch (VenueException e) {
            Assert.fail("Encountered error " + e.getMessage());
        }
    }

    /**
     * Tests seat quality of makeSeatsAvailable method in {@code Venue} when seats are cancelled.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetAvailableSeatsAfterCancel() {
        Venue venue = new Venue(seats);
        int initialSize = venue.getNumberOfAvailableSeats();
        try {
            SeatHold seatHold = venue.getAvailableSeats(TEST_EMAIL, 1);
            Assert.assertEquals(seatHold.getSeats().size(), 1);
            venue.cancelSeatHold(seatHold);
            SeatHold newSeatHold = venue.getAvailableSeats(TEST_EMAIL, 1);
            Assert.assertEquals(newSeatHold.getSeats().size(), 1);
            Assert.assertTrue(seatHold.getSeats().get(0).getSeatQuality() == newSeatHold.getSeats().get(0).getSeatQuality()
                && seatHold.getSeats().get(0).getSeatQuality() == newSeatHold.getSeats().get(0).getSeatQuality()
                && seatHold.getSeats().get(0).getSeatQuality() == newSeatHold.getSeats().get(0).getSeatQuality());
        } catch (VenueException e) {
            Assert.fail("Encountered error " + e.getMessage());
        }
    }

}
