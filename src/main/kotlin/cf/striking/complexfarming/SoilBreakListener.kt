package cf.striking.complexfarming

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class SoilBreakListener : Listener {
    @EventHandler
    fun onSoilBreak(event: BlockBreakEvent) {
        if (event.block.type == Material.SOIL) {
            TODO("Remove from DB if exists")
        }
    }
}