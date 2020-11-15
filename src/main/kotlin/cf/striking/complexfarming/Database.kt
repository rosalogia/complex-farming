package cf.striking.complexfarming

import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager

class Database(private val connectionUrl: String, private val mainTableName: String, private val mainTableSchema: String) : Closeable {
    val connection: Connection by lazy {
        DriverManager.getConnection(connectionUrl).also {
            createMainTable(it)
        }
    }

    override fun close() {
        connection.close()
    }

    private fun createMainTable(conn: Connection) {
        conn.createStatement().apply {
            executeUpdate("CREATE TABLE IF NOT EXISTS $mainTableName $mainTableSchema")
        }
    }
}

