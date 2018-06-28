package com.krok.ticketscanner;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private NewEventActivity newEventActivity;

    @Before
    public void initActivity() {
        newEventActivity = new NewEventActivity();
    }

    @Test
    public void testProperDate() {
        Calendar dateBefore = Calendar.getInstance();
        Calendar dateAfter = Calendar.getInstance();
        dateAfter.add(Calendar.DATE, 1);
        boolean result = newEventActivity.isDateCorrect(dateBefore, dateAfter);

        Assert.assertTrue(result);
    }

    @Test
    public void testEqualDate() {
        Calendar date = Calendar.getInstance();
        boolean result = newEventActivity.isDateCorrect(date, date);

        Assert.assertTrue(result);
    }

    @Test
    public void testWrongDate() {
        Calendar dateBefore = Calendar.getInstance();
        Calendar dateAfter = Calendar.getInstance();
        dateAfter.add(Calendar.DATE, -1);
        boolean result = newEventActivity.isDateCorrect(dateBefore, dateAfter);

        Assert.assertFalse(result);
    }



}