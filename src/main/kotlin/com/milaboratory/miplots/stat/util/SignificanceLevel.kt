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
package com.milaboratory.miplots.stat.util

enum class SignificanceLevel(val string: String) {
    NS("ns"),
    One("*"),
    Two("**"),
    Three("***");

    companion object {
        fun of(pValue: Double) =
            if (pValue >= 0.05) NS
            else if (pValue < 0.0001) Three
            else if (pValue < 0.001) Two
            else One
    }
}