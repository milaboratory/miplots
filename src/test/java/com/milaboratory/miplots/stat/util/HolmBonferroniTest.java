package com.milaboratory.miplots.stat.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class HolmBonferroniTest {
    @Test
    public void test1() {
        List<Double> result = HolmBonferroniFilter.run(
                Arrays.asList(0.001, 0.1, 0.1, 0.1, 0.02, 0.02, 0.02, 1E-4),
                d -> d,
                0.05);
        Assertions.assertEquals(Arrays.asList(1E-4, 0.001), result);
    }
}
