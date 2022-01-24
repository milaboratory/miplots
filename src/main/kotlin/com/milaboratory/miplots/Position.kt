package com.milaboratory.miplots

import com.milaboratory.miplots.Position.Bottom
import com.milaboratory.miplots.Position.Top

enum class Position { Top, Right, Bottom, Left }

val Position.isTopBottom get() = (this == Top) || (this == Bottom)
val Position.isLeftRight get() = !isTopBottom
