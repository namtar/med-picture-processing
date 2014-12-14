package de.htw.berlin.student.gespic;

import de.htw.berlin.student.gespic.utils.StructureElement;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author by Matthias Drummer on 14.12.2014
 */
public class StructureElementTest {

    @Test
    public void testCenterCoordinate() {

        StructureElement element3x3 = new StructureElement(3);

        Assert.assertEquals(element3x3.getCenterCoordinate(), 1);

        StructureElement element5x5 = new StructureElement(5);

        Assert.assertEquals(element5x5.getCenterCoordinate(), 2);

        StructureElement element7x7 = new StructureElement(7);

        Assert.assertEquals(element7x7.getCenterCoordinate(), 3);

    }
}
