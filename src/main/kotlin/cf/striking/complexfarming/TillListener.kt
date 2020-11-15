package cf.striking.complexfarming

import cf.striking.complexfarming.Main.Companion.db
import cf.striking.complexfarming.Main.Companion.log
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.sql.SQLException

class TillListener : Listener {
    private val hoes = listOf(Material.WOOD_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLD_HOE, Material.DIAMOND_HOE)
    private val tillables = listOf(Material.DIRT, Material.GRASS, Material.GRASS_PATH)
    private val crops = listOf(Material.CROPS, Material.WHEAT, Material.CARROT, Material.POTATO)

    @EventHandler
    fun onTillSoil(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.item?.type ?: return in hoes) {
            log.info("Block: ${event.clickedBlock.type}\nAction: ${event.action}\nItem: ${event.item.type}\n" +
                    "inTillables: ${event.clickedBlock.type in tillables}\ninCrops: ${event.clickedBlock.type in crops}\nHumidity: ${event.clickedBlock.humidity}\n" +
                    "Temperature: ${event.clickedBlock.temperature}")
            when (event.clickedBlock.type) {
                in tillables -> {try {
                    val soilCoords = Triple(event.clickedBlock.location.blockX, event.clickedBlock.location.blockY, event.clickedBlock.location.blockZ)
                    insertSoil(soilCoords)

                } catch (exception: SQLException) {
                    log.severe(exception.message)
                }}

                in crops -> {
                    try {
                        val soilCoords = Triple(event.clickedBlock.location.blockX, event.clickedBlock.location.blockY - 1, event.clickedBlock.location.blockZ)
                        insertSoil(soilCoords)
                        val getNutrients = db.connection.createStatement().executeQuery("SELECT nitro, micro, moisture FROM soil WHERE (x, y, z) = (${soilCoords.queryString()})").apply {
                            if (next()) {
                                event.player.sendMessage(
                                        "${ChatColor.GOLD}Nitrogen: ${getInt("nitro")}/127\n" +
                                                "${ChatColor.GREEN}Microclimate: ${getInt("micro")}/127\n" +
                                                "${ChatColor.AQUA}Moisture: ${getInt("moisture")}/127"
                                )
                            }
                        }
                    } catch (exception: SQLException) {
                        log.severe(exception.message)
                    }}

                null -> return
                else -> return

            }
        }
    }
}