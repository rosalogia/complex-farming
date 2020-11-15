package cf.striking.complexfarming

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Logger

class Main : JavaPlugin() {

    companion object {
        val db = Database(
                "jdbc:sqlite:soilstates.db",
                "soil",
                "(x integer, y integer, z integer, nitro integer, micro integer, moisture integer, constraint coord primary key (x, y, z))"
        )

        val log = Logger.getLogger("Minecraft")
    }

    override fun onEnable() {

        val dbFile = File("soilstates.db").apply {
            createNewFile()
        }

        server.pluginManager.registerEvents(TillListener(), this)
        server.pluginManager.registerEvents(GrowthListener(), this)
    }

    override fun onDisable() {}


}