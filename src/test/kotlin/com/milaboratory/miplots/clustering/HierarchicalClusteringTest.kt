package com.milaboratory.miplots.clustering

import com.milaboratory.miplots.clustering.HierarchicalClustering.clusterize
import com.milaboratory.miplots.dendro.Node
import com.milaboratory.miplots.dendro.adjustHeight
import com.milaboratory.miplots.dendro.mapId
import com.milaboratory.miplots.dendro.normalize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

class HierarchicalClusteringTest {
    @Test
    fun test1() {
        val vectors = listOf(
            doubleArrayOf(4.0, 5.0, 2.0, 9.0, 5.0, 6.0, 3.0, 3.0, 1.0),
            doubleArrayOf(4.0, 5.0, 3.0, 8.0, 6.0, 7.0, 8.0, 3.0, 2.0),
            doubleArrayOf(1.0, 2.0, 4.0, 7.0, 5.0, 6.0, 3.0, 6.0, 3.0),
            doubleArrayOf(5.0, 3.0, 6.0, 6.0, 3.0, 4.0, 4.0, 2.0, 4.0),
            doubleArrayOf(2.0, 6.0, 7.0, 5.0, 8.0, 9.0, 8.0, 4.0, 5.0),
            doubleArrayOf(6.0, 6.0, 6.0, 4.0, 6.0, 0.0, 6.0, 6.0, 6.0),
            doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0),
            doubleArrayOf(9.0, 8.0, 7.0, 6.0, 5.0, 6.0, 7.0, 8.0, 9.0),
            doubleArrayOf(0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 6.0),
            doubleArrayOf(0.0, 0.0, 0.0, 3.0, 1.0, 0.0, 0.0, 1.0, 7.0),
            doubleArrayOf(3.0, 7.0, 7.0, 2.0, 7.0, 5.0, 3.0, 4.0, 8.0)
        )
        val r = clusterize(
            vectors,
            0.0,
            HierarchicalClustering::EuclideanDistance
        )
        for (hierarchyNode in r) {
            println(hierarchyNode)
        }
        assert(r.size == 10)

        println(r.asTree())
    }

    @Test
    fun test2() {
        val vectors = listOf(
            doubleArrayOf(1.0, 1.0, 1.0, 1.0),
            doubleArrayOf(1.0, 1.0, 1.0, 1.0),
            doubleArrayOf(1.0, 1.0, 1.0, 1.0),
            doubleArrayOf(1.0, 1.0, 1.0, 1.0)
        )
        val r = clusterize(
            vectors,
            0.0,
            HierarchicalClustering::EuclideanDistance
        )

        Assertions.assertEquals(1, r.size)
        Assertions.assertEquals(1, r.asTree().depth)
    }

    @Test
    fun test3() {
        val vectors = listOf(
            doubleArrayOf(1.0, 1.0, 3.0), // A-0
            doubleArrayOf(1.5, 1.5, 5.0), // B=1
            doubleArrayOf(5.0, 5.0, 1.0), // C=2
            doubleArrayOf(3.0, 4.0, 9.0), // D=3
            doubleArrayOf(4.0, 4.0, 5.0), // E=4
            doubleArrayOf(3.0, 3.5, 4.0)  // F=5
        )
        val r = clusterize(
            vectors,
            0.0,
            HierarchicalClustering::EuclideanDistance
        )
        Assertions.assertEquals(5, r.size)

        val tree = r.asTree()
            .mapId { if ((it ?: 0) < 0) null else it }
            .adjustHeight { (100 * it).roundToInt() / 100.0 }
            .normalize()

        Assertions.assertEquals(4, tree.depth)

        val expected = Node(0.0) {
            Node(4.12) {
                Node(3.91) {
                    Node(2.69) {
                        Node(0, 2.12)
                        Node(1, 2.12)
                    }

                    Node(2.69) {
                        Node(4, 1.5)
                        Node(5, 1.5)
                    }
                }
                Node(2, 3.91)
            }
            Node(3, 4.12)
        }

        Assertions.assertEquals(expected, tree)
    }
}
