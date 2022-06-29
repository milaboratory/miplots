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
package com.milaboratory.miplots

import com.milaboratory.miplots.Position.Bottom
import com.milaboratory.miplots.Position.Top

enum class Position { Top, Right, Bottom, Left }

val Position.isTopBottom get() = (this == Top) || (this == Bottom)
val Position.isLeftRight get() = !isTopBottom

enum class Orientation { Vertical, Horizontal }