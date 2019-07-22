package de.bcersows.photooverlay.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NewLocationTest {

    @Test
    public void testLocationCalculation() {
        final NewLocation oldLocation = new NewLocation(300, 400, 300, 400, 0, 0);

        final NewLocation updatedNewLocation = new NewLocation(oldLocation, 330, 440, 40, 50);
        assertEquals("X correctly updated.", 290D, updatedNewLocation.getUpdatedX(), 1D);
        assertEquals("Y correctly updated.", 390D, updatedNewLocation.getUpdatedY(), 1D);
    }
}
