package de.skyslycer.bookrules.core;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.YamlFileWriter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    public String host;
    public int port;
    public String username;
    public String password;
    public String database;
    public String databasePrefix;

    MessageManager messageManager;

    public void instantiateConfig(YamlFileWriter configFile) {
        if (configFile.getString("mysql.host") == null) {
            configFile.setValue("mysql.host", "localhost");
        } else host = configFile.getString("mysql.host");

        if (configFile.getString("mysql.port") == null) {
            configFile.setValue("mysql.port", 3306);
        } else port = configFile.getInt("mysql.port");

        if (configFile.getString("mysql.username") == null) {
            configFile.setValue("mysql.username", "root");
        } else username = configFile.getString("mysql.username");

        if (configFile.getString("mysql.password") == null) {
            configFile.setValue("mysql.password", "supersecurepassword");
        } else password = configFile.getString("mysql.password");

        if (configFile.getString("mysql.database") == null) {
            configFile.setValue("mysql.database", "bookrules");
        } else database = configFile.getString("mysql.database");

        if (configFile.getString("mysql.prefix") == null) {
            configFile.setValue("mysql.prefix", "br_");
        } else databasePrefix = configFile.getString("mysql.prefix");

        host = configFile.getString("mysql.host");
        port = configFile.getInt("mysql.port");
        username = configFile.getString("mysql.username");
        password = configFile.getString("mysql.password");
        database = configFile.getString("mysql.database");
        databasePrefix = configFile.getString("mysql.prefix");
    }

    public DataSource initMySQLDataSource(MessageManager messageManager, BookRules plugin) throws SQLException {
        this.messageManager = messageManager;

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

        dataSource.setServerName(host);
        dataSource.setPassword(password);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(database);
        dataSource.setUser(username);

        testDataSource(dataSource, plugin);
        return dataSource;
    }

    private void testDataSource(DataSource dataSource, BookRules plugin) {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4Could not connect to database! Please check your database/credentials.");
                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (all.isOp()) {
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4Could not connect to database! Please check your database/credentials.", all);
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4The MySQL connection for the BookRules plugin failed, please contact an administrator!\n§7In case you have access to the server, please check your console and fix the errors!", all);
                }
            }
        } catch (SQLException e) {
            plugin.isConfigSuccessful = false;
            logSQLError(e);
        }
        messageManager.sendDebug("§aSuccessfully established MySQL connection!");
    }

    public void createTable(BookRules plugin, DataSource dataSource) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + databasePrefix + "players (player_uuid CHAR(36) NOT NULL);")) {
                statement.execute();
            } catch (SQLException e) {
                plugin.isConfigSuccessful = false;
                logSQLError(e);
            }
        });
    }

    public void logSQLError(SQLException ex) {
        messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4An error occurred while executing an SQL statement:\n" + ex);
        ex.printStackTrace();
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (all.isOp() || all.hasPermission("bookrules.reload") || all.hasPermission("bookrules.commands") || all.hasPermission("*")) {
                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4The MySQL connection generated an exception! \nPlease check your console!", all);
            } else
                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4The MySQL connection for the BookRules plugin failed, please contact an administrator!\n§7In case you have access to the server, please check your console and fix the errors!", all);
        }
    }
}
