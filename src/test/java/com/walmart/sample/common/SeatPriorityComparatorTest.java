package com.walmart.sample.common;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SeatPriorityComparatorTest {

    /**
     * The comparator being tested.
     */
    private SeatPriorityComparator comparator = new SeatPriorityComparator();

    /**
     * Reference seat
     */
    private Seat referenceSeat = null;

    /**
     * Create a reference seat
     */
    @BeforeClass
    public void setUp() {
        referenceSeat = Seat.builder().seatQuality(10).seatNumber(10).rowNumber(10).build();
    }

    /**
     * Tests comparing seats that are naturally equal.
     */
    @Test(groups = {"fast", "unit"})
    public void testNaturalEquality() {
        Assert.assertEquals(0, comparator.compare(referenceSeat, referenceSeat), "Natural equality isn't equal");
    }

    /**
     * Tests comparing reference seat to lower quality.
     */
    @Test(groups = {"fast", "unit"})
    public void testLowerQualitySeat() {
        Seat poorQualitySeat = Seat.builder().seatQuality(9).seatNumber(10).rowNumber(10).build();
        Assert.assertEquals(-1, comparator.compare(referenceSeat, poorQualitySeat), "Poor Quality Seat is greater than reference");
    }

    /**
     * Tests comparing reference seat to higher quality.
     */
    @Test(groups = {"fast", "unit"})
    public void testHighQualitySeat() {
        Seat highQualitySeat = Seat.builder().seatQuality(11).seatNumber(10).rowNumber(10).build();
        Assert.assertEquals(1, comparator.compare(referenceSeat, highQualitySeat), "High Quality Seat is lower than reference");
    }

    /**
     * Tests comparing reference seat same quality, higher row
     */
    @Test(groups = {"fast", "unit"})
    public void testSameQualityHigherRow() {
        Seat highQualitySeat = Seat.builder().seatQuality(10).seatNumber(10).rowNumber(11).build();
        Assert.assertEquals(-1, comparator.compare(referenceSeat, highQualitySeat), "Same Quality, higher row than reference");
    }

    /**
     * Tests comparing reference seat same quality, lower row
     */
    @Test(groups = {"fast", "unit"})
    public void testSameQualityLowerRow() {
        Seat poorQualitySeat = Seat.builder().seatQuality(10).seatNumber(10).rowNumber(9).build();
        Assert.assertEquals(1, comparator.compare(referenceSeat, poorQualitySeat), "Same Quality, lower row than reference");
    }

    /**
     * Tests comparing reference seat same quality and row, higher seat number
     */
    @Test(groups = {"fast", "unit"})
    public void testSameQualityAndRowHigherSeat() {
        Seat highQualitySeat = Seat.builder().seatQuality(10).seatNumber(11).rowNumber(10).build();
        Assert.assertEquals(-1, comparator.compare(referenceSeat, highQualitySeat), "Same quality and row, higher seat number than reference.");
    }

    /**
     * Tests comparing reference seat same quality and row, lower seat number
     */
    @Test(groups = {"fast", "unit"})
    public void testSameQualityAndRowLowerSeat() {
        Seat poorQualitySeat = Seat.builder().seatQuality(10).seatNumber(9).rowNumber(10).build();
        Assert.assertEquals(1, comparator.compare(referenceSeat, poorQualitySeat), "Same quality and row, lower seat number than reference.");
    }
}
