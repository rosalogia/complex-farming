package cf.striking.complexfarming

import cf.striking.complexfarming.Main.Companion.db
import java.util.Random

val rand = Random()

fun <A, B, C> Triple<A, B, C>.queryString() = "${this.first}, ${this.second}, ${this.third}"

fun <T> List<T>.random() = this.elementAt(rand.nextInt(this.size))

fun insertSoil(soilCoords: Triple<Int, Int, Int>) {
    db.connection.prepareStatement("INSERT OR IGNORE INTO soil (x, y, z, nitro, micro, moisture) VALUES (?, ?, ?, 20, 20, 20)").apply {
        setInt(1, soilCoords.first)
        setInt(2, soilCoords.second)
        setInt(3, soilCoords.third)
        executeUpdate()
    }
}