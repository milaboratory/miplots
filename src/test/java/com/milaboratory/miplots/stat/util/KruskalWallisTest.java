/*
 *
 * Copyright (c) 2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/miplots/blob/main/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
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
