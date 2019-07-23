package de.bcersows.photooverlay.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DragDeltaTest {

    @Test
    public void testDeltaCalculation() {
        final DragDelta oldLocation = new DragDelta(0, 0, 300, 400);

        final DragDelta updatedNewLocation = new DragDelta(oldLocation, 330, 390);
        assertEquals("X correctly updated.", 30D, updatedNewLocation.getDeltaX(), 1D);
        assertEquals("Y correctly updated.", -10D, updatedNewLocation.getDeltaY(), 1D);
    }
}
