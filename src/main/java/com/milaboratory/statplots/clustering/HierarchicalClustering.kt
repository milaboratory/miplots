package com.milaboratory.statplots.clustering

import kotlin.math.abs

/**
 *
 */
object HierarchicalClustering {
    fun EuclideanDistance(vectori: DoubleArray, vectorj: DoubleArray): Double {
        var diff_square_sum = 0.0
        for (i in vectori.indices) {
            diff_square_sum += (vectori[i] - vectorj[i]) * (vectori[i] - vectorj[i])
        }
        return Math.sqrt(diff_square_sum)
    }

    fun ManhattenDistance(vectori: DoubleArray, vectorj: DoubleArray): Double {
        var abs_sum = 0.0
        for (i in vectori.indices) {
            abs_sum += abs(vectori[i] - vectorj[i])
        }
        return abs_sum
    }

    fun ChebishevDistance(vectori: DoubleArray, vectorj: DoubleArray): Double {
        var max_distance = 0.0
        for (i in vectori.indices) {
            val distance = abs(vectori[i]) - abs(vectorj[i])
            if (distance >= max_distance) {
                max_distance = distance
            }
        }
        return max_distance
    }

    fun <T> clusterize(
        vectors: List<T>,
        distanceOffset: Double,
        distanceFunc: (T, T) -> Double
    ): List<HierarchyNode> {
        if (vectors.size == 0) return emptyList()
        if (vectors.size == 1) return listOf(HierarchyNode(0, emptyList(), 0.0))
        val result: MutableList<HierarchyNode> = ArrayList()
        val distances: MutableList<PairDistance> = ArrayList()
        val clusters = mutableMapOf<Int, List<Int>>()
        val rawDist = Array(vectors.size) { DoubleArray(vectors.size) }
        require(distanceOffset <= 1) { "Offset must be less then 1" }
        for (i in vectors.indices) {
            clusters[i] = listOf(i)
            for (j in i + 1 until vectors.size) {
                val distance = PairDistance(
                    i, j, distanceFunc(
                        vectors[i],
                        vectors[j]
                    )
                )
                distances.add(distance)
                rawDist[i][j] = distance.distance
                rawDist[j][i] = distance.distance
            }
        }
        distances.sort()
        var id = -1
        while (true) {
            val children = mutableListOf<Int>()
            val childrenNeighbors = mutableSetOf<Int>()
            val currentNodeDistance = distances[0].distance
            var distanceSum = distances[0].distance
            children.add(distances[0].id1)
            children.add(distances[0].id2)
            for (i in 1 until distances.size) {
                if (distances[i].distance <= currentNodeDistance * (1 + distanceOffset)) {
                    for (j in 0 until children.size) {
                        if (distances[i].id1 == children.get(j)) {
                            childrenNeighbors.add(distances[i].id2)
                            distanceSum += distances[i].distance
                        } else if (distances[i].id2 == children.get(j)) {
                            childrenNeighbors.add(distances[i].id1)
                            distanceSum += distances[i].distance
                        }
                    }
                }
            }
            children.addAll(childrenNeighbors)
            val node = HierarchyNode(id, children, distanceSum / (children.size - 1))
            result.add(node)
            if (distances.size == 1) {
                return result
            }
            val allChildrenList = mutableListOf<Int>()
            for (i in 0 until children.size) {
                val c = clusters.remove(children[i])
                allChildrenList.addAll(c!!)
            }
            for (i in distances.indices.reversed()) {
                for (j in 0 until children.size) {
                    val child: Int = children[j]
                    if (distances[i].id1 == child || distances[i].id2 == child) {
                        distances.removeAt(i)
                        break
                    }
                }
            }
            val it = clusters.iterator()
            while (it.hasNext()) {
                val e = it.next()
                val id1 = id
                val id2: Int = e.key
                var minDistance = Double.MAX_VALUE
                val children2 = e.value
                for (i in allChildrenList) {
                    for (j in children2) {
                        if (rawDist[i][j] < minDistance) {
                            minDistance = rawDist[i][j]
                        }
                    }
                }
                distances.add(PairDistance(id1, id2, minDistance))
            }
            if (distances.isEmpty()) return result
            clusters[id] = allChildrenList
            distances.sort()
            id--
        }
    }

    private class PairDistance constructor(val id1: Int, val id2: Int, val distance: Double) :
        Comparable<PairDistance> {
        override operator fun compareTo(other: PairDistance): Int {
            return distance.compareTo(other.distance)
        }
    }
}
