package com.milaboratory.miplots.stat.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
class KruskalWallisTest {
    @Test
    void test1() {
        KruskalWallis kw = new KruskalWallis();
        double p = kw.kruskalWallisTest(
                new double[]{1.0, 23.23, 34.5},
                new double[]{12.0, 123.23, 3.5, 32, 32},
                new double[]{13.0, 3.23, 234.5, 34.2}
        );
        Assertions.assertEquals(0.8860909, p, 1e-5);
    }
}
