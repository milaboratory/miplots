package com.milaboratory.statplots.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class KendallsCorrelationTest {

    @Test
    void test1() {
        double[] x = {44.4, 45.9, 41.9, 53.3, 44.7, 44.1, 50.7, 45.2, 60.1};
        double[] y = {2.6, 3.1, 2.5, 5.0, 3.6, 4.0, 5.2, 2.8, 3.8};

        KendallsCorrelation corr = new KendallsCorrelation(x, y);
        assertEquals(0.4444, corr.tau, 1e-3);
    }

    @Test
    void test2() {
        double[] x = {1, 2, 3};
        double[] y = {3, 4, 5};

        KendallsCorrelation corr = new KendallsCorrelation(x, y);
        assertEquals(1.5666989036012806, corr.significance);
    }

    @Test
    void test4() {
        double[] x = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        double[] y = {1, 2, 3, 5, 4, 7, 6, 8, 10, 9, 11, 12};

        KendallsCorrelation corr = new KendallsCorrelation(x, y);
        assertEquals(0.909, corr.tau, 1e-3);
        assertEquals(4.114, corr.significance, 1e-3);
        assertEquals(0.00004, corr.pValue, 1e-5);
    }
}
