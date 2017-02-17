package com.walmart.sample.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Seat class represents a single reservable seat.
 */
@Getter
@Setter
@Builder
@ToString
public class Seat {

    /**
     * The row number of the seat.
     */
    private int rowNumber;

    /**
     * The seat number within a row.
     */
    private int seatNumber;

    /**
     * The quality of the seat within the venue.
     */
    private int seatQuality;

}
