package com.walmart.sample.ticketing.service;

import com.walmart.sample.common.Customer;
import com.walmart.sample.common.Reservation;
import com.walmart.sample.common.ReservationState;
import com.walmart.sample.common.Seat;
import com.walmart.sample.common.Venue;
import com.walmart.sample.common.VenueException;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for TicketingServiceImpl
 */
@Slf4j
public class TicketingServiceImplTest {

    /**
     * Initial seats used in venue creation
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
     * TicketingService being tested.
     */
    private TicketingService ticketService = new TicketingServiceImpl();

    /**
     * Customer used in tests
     */
    private Customer customer;

    /**
     * Create a simple seat list for venue creation in test cases.
     */
    @BeforeClass
    public void setUp() {
        for (int seatNumber = 1; seatNumber <= SEATS_PER_ROW; seatNumber++) {
            for (int row = 1; row <= ROWS; row++) {
                //TODO Simplistic seat quality algorithm with row and seat number.  Only impacts Comparator if changed.
                seats.add(Seat.builder().rowNumber(row).seatNumber(seatNumber).seatQuality(
                    1000000 - (ROWS - row * 1000 + SEATS_PER_ROW - seatNumber)).build());
            }
        }

        customer = Customer.builder().firstName("Homer").lastName("Simpson").build();
    }

    /**
     * Tests getNumberOfAvailableSeats method in {@code TicketingServiceImpl} and the cancellation after threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeats() {

        Venue venue = new Venue(seats);
        int initialAvailableSeats = ticketService.getNumberOfAvailableSeats(venue);
        Assert.assertEquals(seats.size(), initialAvailableSeats);

        List<Reservation> reservations = new ArrayList<Reservation>();

        for (int i = 0; i < 10; i++) {
            try {
                reservations.add(ticketService.holdBestAvailableSeats(customer, venue, 5));
            } catch (VenueException e) {
                Assert.fail("Unexpected VenueException thrown");
            }
        }
        int reservedSeats = 0;
        for (Reservation reservation : reservations) {
            reservedSeats += reservation.getSeats().size();
        }
        Assert.assertEquals(initialAvailableSeats - reservedSeats, ticketService.getNumberOfAvailableSeats(venue));

        // Wait until hold expires
        try {
            Thread.sleep((TicketingService.HOLD_EXPIRATION_SECONDS + 1) * 1000);
        } catch (InterruptedException e) {
            Assert.fail("Failed to sleep beyond HOLD_EXPIRATION_SECONDS");
        }

        // All seats should be available
        Assert.assertEquals(initialAvailableSeats, ticketService.getNumberOfAvailableSeats(venue));
    }


    /**
     * Tests getNumberOfAvailableSeats and cancelReservation method in {@code TicketingServiceImpl} before cancel threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeatsAfterCancel() {

        Venue venue = new Venue(seats);
        int initialAvailableSeats = ticketService.getNumberOfAvailableSeats(venue);
        Assert.assertEquals(seats.size(), initialAvailableSeats);

        List<Reservation> reservations = new ArrayList<Reservation>();
        List<Reservation> reservationsToCancel = new ArrayList<Reservation>();

        try {
            for (int i = 0; i < 10; i++) {
                Reservation reservation = ticketService.holdBestAvailableSeats(customer, venue, 5);
                reservations.add(reservation);
                if (i % 2 == 0) {
                    reservationsToCancel.add(reservation);
                }
            }
            // Get reserved seat count before cancel.
            int reservedSeatCnt = 0;
            for (Reservation reservation : reservations) {
                reservedSeatCnt += reservation.getSeats().size();
            }
            Assert.assertEquals(initialAvailableSeats - reservedSeatCnt, ticketService.getNumberOfAvailableSeats(venue));

            int cancelledSeats = 0;
            for (Reservation reservation : reservationsToCancel) {
                ticketService.cancelReservation(reservation);
                cancelledSeats += reservation.getSeats().size();
            }

            // Available seats should equal initial seats - reservedSeatCnt
            Assert.assertEquals(initialAvailableSeats - (reservedSeatCnt - cancelledSeats), ticketService.getNumberOfAvailableSeats(venue));

        } catch (VenueException e) {
            Assert.fail("Unexpected VenueException thrown");
        }
    }



    /**
     * Tests getNumberOfAvailableSeats and confirmReservation method in {@code TicketingServiceImpl} before cancel threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testGetNumberOfAvailableSeatsAfterConfirm() {

        Venue venue = new Venue(seats);
        List<Reservation> reservations = new ArrayList<Reservation>();

        try {

            int initialSeatCount = ticketService.getNumberOfAvailableSeats(venue);

            for (int i = 0; i < 10; i++) {
                reservations.add(ticketService.holdBestAvailableSeats(customer, venue, 5));
            }

            int remainingSeatCount = ticketService.getNumberOfAvailableSeats(venue);


            // Check for reservations in hold state
            for (Reservation reservation: reservations) {
                Assert.assertEquals(reservation.getState(), ReservationState.HOLD);
                ticketService.confirmReservation(reservation);
            }

            // Wait until hold expires
            try {
                Thread.sleep((TicketingService.HOLD_EXPIRATION_SECONDS + 1) * 1000);
            } catch (InterruptedException e) {
                Assert.fail("Failed to sleep beyond HOLD_EXPIRATION_SECONDS");
            }

            // Check for reservations in reserved state
            for (Reservation reservation: reservations) {
                Assert.assertEquals(reservation.getState(), ReservationState.RESERVED);
            }

            // Verify no reservations were cancelled
            Assert.assertEquals(remainingSeatCount, ticketService.getNumberOfAvailableSeats(venue));

        } catch (VenueException e) {
            Assert.fail("Unexpected VenueException thrown");
        }
    }
    /**
     * Tests the holdBestAvailableSeats method in {@code TicketingServiceImpl}.
     */
    @Test(groups = {"fast", "unit"})
    public void testHoldBestAvailableSeats() {

        Venue venue = new Venue(seats);
        List<Reservation> reservations = new ArrayList<Reservation>();

        int initialSeatCount = ticketService.getNumberOfAvailableSeats(venue);
        for (int i = 0; i < 10; i++) {
            try {
                reservations.add(ticketService.holdBestAvailableSeats(customer, venue, 5));
            } catch (VenueException e) {
                Assert.fail("Unexpected VenueException thrown");
            }
        }

        // Check for reservations in hold state
        int holdSeatCount = 0;
        for (Reservation reservation : reservations) {
            holdSeatCount += reservation.getSeats().size();
            Assert.assertEquals(reservation.getState(), ReservationState.HOLD);
        }

        Assert.assertEquals(ticketService.getNumberOfAvailableSeats(venue), initialSeatCount - holdSeatCount);
    }


    /**
     * Tests testHoldBestAvailableSeats and cancelReservation method in {@code TicketingServiceImpl}, initial hold and then cancellation after threshold.
     */
    @Test(groups = {"fast", "unit"})
    public void testHoldSeatAndHoldExpiration() {

        Venue venue = new Venue(seats);
        List<Reservation> reservations = new ArrayList<Reservation>();

        int initialSeatCount = ticketService.getNumberOfAvailableSeats(venue);

        for (int i = 0; i < 10; i++) {
            try {
                reservations.add(ticketService.holdBestAvailableSeats(customer, venue, 5));
            } catch (VenueException e) {
                Assert.fail("Unexpected VenueException thrown");
            }
        }

        // Check for reservations in hold state
        for (Reservation reservation: reservations) {
            Assert.assertEquals(reservation.getState(), ReservationState.HOLD);
        }

        // Wait until hold expires
        try {
            Thread.sleep((TicketingService.HOLD_EXPIRATION_SECONDS + 1) * 1000);
        } catch (InterruptedException e) {
            Assert.fail("Failed to sleep beyond HOLD_EXPIRATION_SECONDS");
        }

        // Check for reservations in hold state
        for (Reservation reservation: reservations) {
            Assert.assertEquals(reservation.getState(), ReservationState.CANCELLED);
        }


        Assert.assertEquals(ticketService.getNumberOfAvailableSeats(venue), initialSeatCount);
    }

}
