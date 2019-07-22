package de.bcersows.photooverlay.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CalculatedImageSizeTest {

    @Test
    public void testCalculate() throws Exception {
        CalculatedImageSize calculated;

        calculated = CalculatedImageSize.calculate(200, 300);
        assertEquals("Under limit, all good.", 300, calculated.getHeight(), 1);
        assertEquals("Under limit, all good.", 200, calculated.getWidth(), 1);

        calculated = CalculatedImageSize.calculate(1000, 400);
        assertEquals("Height got down-sized correctly.", 200, calculated.getHeight(), 1);
        assertEquals("Width got capped at max.", 500, calculated.getWidth(), 1);

        calculated = CalculatedImageSize.calculate(400, 1000);
        assertEquals("Height got capped at max.", 500, calculated.getHeight(), 1);
        assertEquals("Width got down-sized correctly.", 200, calculated.getWidth(), 1);

        calculated = CalculatedImageSize.calculate(2000, 1000);
        assertEquals("Height got down-sized correctly.", 250, calculated.getHeight(), 1);
        assertEquals("Width got capped at max.", 500, calculated.getWidth(), 1);

        calculated = CalculatedImageSize.calculate(1000, 2000);
        assertEquals("Height got capped at max.", 500, calculated.getHeight(), 1);
        assertEquals("Width got down-sized correctly.", 250, calculated.getWidth(), 1);
    }

}
