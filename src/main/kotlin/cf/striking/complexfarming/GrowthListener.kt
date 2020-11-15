package cf.striking.complexfarming

import cf.striking.complexfarming.Main.Companion.db
import cf.striking.complexfarming.Main.Companion.log
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockGrowEvent
import java.sql.SQLException

class GrowthListener : Listener {

    val growables = listOf(Material.CROPS, Material.CARROT, Material.POTATO)

    private val hot = listOf(
            Biome.BEACHES, Biome.DESERT, Biome.DESERT_HILLS, Biome.MUTATED_DESERT, Biome.JUNGLE, Biome.JUNGLE_HILLS,
            Biome.JUNGLE_EDGE, Biome.MUTATED_JUNGLE, Biome.MUTATED_JUNGLE_EDGE, Biome.MESA, Biome.MESA_CLEAR_ROCK,
            Biome.MESA_ROCK, Biome.MUTATED_MESA, Biome.MUTATED_MESA_CLEAR_ROCK, Biome.MUTATED_MESA_ROCK, Biome.SAVANNA,
            Biome.SAVANNA_ROCK, Biome.MUTATED_SAVANNA, Biome.MUTATED_SAVANNA_ROCK
    )

    private val neutral = listOf(
            Biome.BIRCH_FOREST, Biome.BIRCH_FOREST_HILLS, Biome.BIRCH_FOREST_HILLS, Biome.MUTATED_BIRCH_FOREST,
            Biome.MUTATED_BIRCH_FOREST_HILLS, Biome.FOREST, Biome.FOREST_HILLS, Biome.MUTATED_FOREST, Biome.ROOFED_FOREST,
            Biome.MUTATED_ROOFED_FOREST, Biome.MUSHROOM_ISLAND, Biome.MUSHROOM_ISLAND_SHORE, Biome.PLAINS, Biome.MUTATED_PLAINS,
            Biome.SWAMPLAND, Biome.MUTATED_SWAMPLAND, Biome.STONE_BEACH
    )

    private val cold = listOf(
            Biome.COLD_BEACH, Biome.EXTREME_HILLS, Biome.EXTREME_HILLS_WITH_TREES, Biome.MUTATED_EXTREME_HILLS, Biome.MUTATED_EXTREME_HILLS_WITH_TREES,
            Biome.SMALLER_EXTREME_HILLS, Biome.ICE_FLATS, Biome.ICE_MOUNTAINS, Biome.MUTATED_ICE_FLATS, Biome.TAIGA, Biome.TAIGA_COLD, Biome.TAIGA_COLD_HILLS,
            Biome.TAIGA_HILLS, Biome.MUTATED_REDWOOD_TAIGA, Biome.MUTATED_REDWOOD_TAIGA_HILLS, Biome.MUTATED_TAIGA, Biome.MUTATED_TAIGA_COLD, Biome.REDWOOD_TAIGA
    )


    @EventHandler
    fun onGrowth(event: BlockGrowEvent) {
        val soil = Location(
                event.block.world,
                event.block.location.blockX.toDouble(),
                (event.block.location.blockY - 1).toDouble(), //The block directly under the plant
                event.block.location.blockZ.toDouble()
        )

        val temperature = (when (event.block.biome) {
            in hot -> 90..120
            in neutral -> 45..90
            in cold -> 0..45
            else -> 1..100
        }).toList()

        val soilCoords = Triple(soil.blockX, soil.blockY, soil.blockZ)


        val updateProperties = when (event.block.type) {
            Material.CROPS -> Triple(2, -1, 0) //Nitro, moisture, micro
            Material.POTATO -> Triple(-1, 0, 2)
            Material.CARROT -> Triple(0, 2, -1)
            else -> return
        }

        try {
            insertSoil(soilCoords)



            val response = db.connection.createStatement().executeQuery("SELECT * FROM soil WHERE (x, y, z) = (${soilCoords.queryString()})")

            while (response.next()) {
                val nutrition = (listOf(
                        response.getInt("nitro"),
                        response.getInt("micro"),
                        response.getInt("moisture")
                ).fold(0, {total, next -> total + next}))/3.0

                val successRate = (nutrition / 127.0) - temperatureDisadvantage(temperature.random())

                val determiner = Math.random()

                if ( determiner > successRate) {
                    event.isCancelled = true
                } else {
                    val updateNutrients = db.connection.prepareStatement("UPDATE soil SET nitro = ?, moisture = ?, micro = ? WHERE (x, y, z) = (?, ?, ?)").apply {
                        setInt(1, response.getInt("nitro") + updateProperties.first)
                        setInt(2, response.getInt("moisture") + updateProperties.second)
                        setInt(3, response.getInt("micro") + updateProperties.third)
                        setInt(4, soilCoords.first)
                        setInt(5, soilCoords.second)
                        setInt(6, soilCoords.third)
                        executeUpdate()
                    }
                }
            }

        } catch (exception: SQLException) {
            log.severe(exception.message)
        }
    }

    private fun temperatureDisadvantage(temperature: Int) = (Math.pow((temperature - 65).toDouble(), 2.0)/95)/100
}