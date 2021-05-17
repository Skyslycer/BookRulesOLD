package de.skyslycer.bookrules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellectualsites.http.EntityMapper;
import com.intellectualsites.http.HttpClient;
import com.intellectualsites.http.external.GsonMapper;
import de.skyslycer.bookrules.api.RulesAPI;
import de.skyslycer.bookrules.commands.AcceptRulesCommand;
import de.skyslycer.bookrules.commands.BookRulesCommand;
import de.skyslycer.bookrules.commands.DeclineRulesCommand;
import de.skyslycer.bookrules.commands.RuleBookCommand;
import de.skyslycer.bookrules.core.BookManager;
import de.skyslycer.bookrules.core.DatabaseManager;
import de.skyslycer.bookrules.core.MessageManager;
import de.skyslycer.bookrules.core.PermissionManager;
import de.skyslycer.bookrules.listener.BlockListener;
import de.skyslycer.bookrules.listener.JoinQuitListener;
import de.skyslycer.bookrules.util.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BookRules extends JavaPlugin {

    public static BookRules instance;

    private final Map<Player, Location> playerCache = new ConcurrentHashMap<>();

    public YamlFileWriter configFile;
    public boolean isConfigSuccessful;

    double latestVersion;
    double currentVersion = 2.0;
    public boolean isLatestVersion;
    public HttpClient httpClient;

    public DataSource dataSource;

    public ArrayList<String> players = new ArrayList<>();

    public StorageType storageType;

    public RulesAPI rulesAPI = new RulesAPI();
    public MessageManager messageManager = new MessageManager();
    public PermissionManager permissionManager = new PermissionManager();
    public DatabaseManager databaseManager = new DatabaseManager();

    BookManager bookManager = new BookManager();
    AcceptRulesCommand acceptRulesCommand = new AcceptRulesCommand();
    BookRulesCommand bookRulesCommand = new BookRulesCommand();
    DeclineRulesCommand declineRulesCommand = new DeclineRulesCommand();
    RuleBookCommand ruleBookCommand = new RuleBookCommand();
    BlockListener blockListener = new BlockListener();
    JoinQuitListener joinQuitListener = new JoinQuitListener();

    @Override
    public void onEnable() {
        instance = this;
        injectData();
        instantiateConfig();
        rulesAPI.getPlayerData();
        instantiateMetrics();
        startThread();
        getLatestVersion();

        getCommand("declinerules").setExecutor(declineRulesCommand);
        getCommand("acceptrules").setExecutor(acceptRulesCommand);
        getCommand("bookrules").setExecutor(bookRulesCommand);
        getCommand("rulebook").setExecutor(ruleBookCommand);

        getCommand("bookrules").setTabCompleter(new BookRulesCommand());

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(joinQuitListener, this);
        pluginManager.registerEvents(blockListener, this);

        if(!configFile.isSuccessful()) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4Bookrules failed to load config! Please correct the errors!", Bukkit.getConsoleSender());
        }else messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§aBookRules §7successfully loaded!", Bukkit.getConsoleSender());
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§aPlaceholderAPI §7successfully registered!", Bukkit.getConsoleSender());
        }else messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4PlaceholderAPI §7couldn't be found! Placeholders are not supported!", Bukkit.getConsoleSender());

        if(MCVersion.getVersion().isOlderThan(MCVersion.v1_12_R1)) {
            messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4You are running an unsupported version of minecraft! Do NOT expect working features/support!");
        }
    }

    public void onDisable() {
        rulesAPI.setPlayerData();
    }

    public void injectData() {
        acceptRulesCommand.injectData(messageManager, playerCache, permissionManager);
        bookRulesCommand.injectData(messageManager, this);
        declineRulesCommand.injectData(messageManager, permissionManager);
        ruleBookCommand.injectData(messageManager, bookManager, permissionManager);
        bookManager.injectData(messageManager, permissionManager);
        blockListener.injectData(bookManager);
        joinQuitListener.injectData(bookManager, this, messageManager, playerCache);
    }

    public void instantiateMetrics() {
        Metrics metrics = new Metrics(this, 11121);
        metrics.addCustomChart(new SimplePie("latest_version", () -> String.valueOf(isLatestVersion)));
        metrics.addCustomChart(new AdvancedPie("storage_types", () -> {
            HashMap<String, Integer> values = new HashMap<>();
            if(storageType == StorageType.LOCAL) {
                values.put("local", 1);
            }else if(storageType == StorageType.MYSQL) {
                values.put("mysql", 1);
            }
            return values;
        }));
    }

    public void getLatestVersion() {
        isLatestVersion = true;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            isLatestVersion = true;
            httpClient = HttpClient.newBuilder()
                    .withBaseURL("https://api.spiget.org")
                    .withEntityMapper(EntityMapper.newInstance().registerDeserializer(JsonObject.class, GsonMapper.deserializer(JsonObject.class, new Gson())))
                    .build();

            httpClient.get("v2/resources/91272/versions/latest")
                    .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36 Edg/90.0.818.42")
                    .onStatus(200, response -> {
                        messageManager.sendDebug("§aSuccessfully received version data.");
                        final JsonObject jsonObject = response.getResponseEntity(JsonObject.class);
                        if(jsonObject.get("name") != null) {
                            latestVersion = Float.parseFloat(jsonObject.get("name").getAsString());
                            if(latestVersion > currentVersion) {
                                isLatestVersion = false;
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_UNSUPPORTED_VERSION);
                            }
                        }
                    })
                    .onStatus(404, response -> messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4ERROR: Could not find the api, please ensure you have a working internet connection or contact the developer."))
                    .onRemaining(response -> messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4ERROR: Got following status code: " + response.getStatusCode() + ". Please ensure you have a working internet connection or contact the developer."))
                    .onException(Throwable::printStackTrace)
                    .execute();
        },20, 72000);
    }

    public void startThread() {
        for (Player all : Bukkit.getOnlinePlayers()) {
            if(!rulesAPI.playerHasAcceptedRules(all.getUniqueId().toString())) {
                playerCache.put(all, all.getLocation());
            }
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> playerCache.forEach((player, location) -> {
            if(rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString())) {
                playerCache.remove(player);
            }else if (player.getLocation().distanceSquared(location) >= 0.1) {
                player.teleport(location);
                bookManager.openBook(player, "bookrules.onclose", false);
            }
        }), 0, 20);
    }

    public boolean instantiateConfig() {
        configFile = new YamlFileWriter("plugins//BookRules", "config.yml");
        isConfigSuccessful = configFile.isSuccessful();
        if(configFile.isSuccessful()) {
            //messages
            messageManager.instantiateMessages(configFile);
            //extra permissions
            permissionManager.instantiatePermissions(configFile, messageManager);
            //storage method
            if(configFile.getString("storage-method") != null) {
                if(configFile.getString("storage-method").equalsIgnoreCase("mysql")) {
                    storageType = StorageType.MYSQL;
                }else storageType = StorageType.LOCAL;
            }else configFile.setValue("storage-method", "local");
            //mysql
            databaseManager.instantiateConfig(configFile);
            //content
            bookManager.instantiateContent(configFile);
            //starting mysql innit if enabled
            if(storageType == StorageType.MYSQL) {
                try {
                    dataSource = databaseManager.initMySQLDataSource(messageManager, this);
                } catch (SQLException e) {
                    databaseManager.logSQLError(e);
                }
                databaseManager.createTable(this, dataSource);
            }
        }
        return isConfigSuccessful;
    }

    public static BookRules getAPIData() {
        return instance;
    }
}
