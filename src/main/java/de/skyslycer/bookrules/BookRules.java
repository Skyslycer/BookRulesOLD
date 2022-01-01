package de.skyslycer.bookrules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import de.skyslycer.bookrules.util.MCVersion;
import de.skyslycer.bookrules.util.StorageType;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BookRules extends JavaPlugin {

    public static BookRules instance;

    private final Map<Player, Location> playerCache = new ConcurrentHashMap<>();

    public boolean isConfigSuccessful;
    public boolean isLatestVersion;
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

    public static BookRules getAPIData() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        injectData();
        isConfigSuccessful = instantiateConfig();
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

        if (!isConfigSuccessful) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4Bookrules failed to load config! Please correct the errors!", Bukkit.getConsoleSender());
        } else {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§aBookRules §7successfully loaded!", Bukkit.getConsoleSender());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§aPlaceholderAPI §7successfully registered!", Bukkit.getConsoleSender());
        } else {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§4PlaceholderAPI §7couldn't be found! Placeholders are not supported!", Bukkit.getConsoleSender());
        }

        if (MCVersion.getVersion().isMajorOlderThan(MCVersion.v1_12_R1.getMajorVersion())) {
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
        databaseManager.injectData(messageManager);
    }

    public void instantiateMetrics() {
        Metrics metrics = new Metrics(this, 11121);
        metrics.addCustomChart(new SimplePie("latest_version", () -> String.valueOf(isLatestVersion)));
        metrics.addCustomChart(new AdvancedPie("versions", () -> {
            HashMap<String, Integer> values = new HashMap<>();
            values.put(String.valueOf(getVersion()), 1);
            return values;
        }));
        metrics.addCustomChart(new AdvancedPie("storage_types", () -> {
            HashMap<String, Integer> values = new HashMap<>();
            if (storageType == StorageType.LOCAL) {
                values.put("local", 1);
            } else if (storageType == StorageType.MYSQL) {
                values.put("mysql", 1);
            }
            return values;
        }));
    }

    public void getLatestVersion() {
        HttpClient client = HttpClient.newHttpClient();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            isLatestVersion = true;
            try {
                var request = client.send(HttpRequest.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36 Edg/90.0.818.42")
                        .uri(URI.create("https://api.spiget.org/v2/resources/91272/versions/latest"))
                        .build(), HttpResponse.BodyHandlers.ofInputStream());
                switch (request.statusCode()) {
                    case 200 -> {
                        try {
                            messageManager.sendDebug("§aSuccessfully received version data.");
                            JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(request.body()), JsonObject.class);
                            if (jsonObject.get("name") != null) {
                                var latestVersion = Integer.parseInt(jsonObject.get("name").getAsString().replace(".", ""));
                                if (latestVersion > getVersion()) {
                                    isLatestVersion = false;
                                    messageManager.sendDebug(MessageManager.DebugType.DEBUG_UNSUPPORTED_VERSION);
                                }
                            }
                        } catch (Exception ignored) {
                            isLatestVersion = false;
                            messageManager.sendDebug(MessageManager.DebugType.DEBUG_UNSUPPORTED_VERSION);
                        }
                    }
                    case 404 -> messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4ERROR: Could not find the api, please ensure you have a working internet connection or contact the developer.");
                    default -> messageManager.sendDebug(MessageManager.DebugType.DEBUG_WARN, "§4ERROR: Got following status code: " + request.statusCode() + ". Please ensure you have a working internet connection or contact the developer.");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, 20, 72000);
    }

    public void startThread() {
        for (Player all : Bukkit.getOnlinePlayers()) {
            rulesAPI.playerHasAcceptedRules(all.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                if (!hasAccepted) playerCache.put(all, all.getLocation());
            });
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> playerCache.forEach((player, location) -> rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()).thenAccept((hasAccepted) -> {
            if (hasAccepted) {
                playerCache.remove(player);
                return;
            }

            if (player.getLocation().distanceSquared(location) >= 0.1) {
                player.teleport(location);
                bookManager.openBook(player, "bookrules.onclose", false);
            }
        })), 0, 20);
    }

    public boolean instantiateConfig() {
        Path configPath = Paths.get(getDataFolder().getPath(), "config.yml");
        saveDefaultConfig();

        try {
            YamlConfiguration.loadConfiguration(configPath.toFile());
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        //messages
        messageManager.instantiateMessages(getConfig(), configPath);

        //extra permissions
        permissionManager.instantiatePermissions(getConfig(), configPath, messageManager);

        //storage method
        if (getConfig().getString("storage-method") != null) {
            if (getConfig().getString("storage-method").equalsIgnoreCase("mysql")) {
                storageType = StorageType.MYSQL;
            } else storageType = StorageType.LOCAL;
        } else getConfig().set("storage-method", "local");

        //mysql
        databaseManager.instantiateConfig(getConfig(), configPath);

        //content
        bookManager.instantiateContent(getConfig(), configPath);

        //starting mysql innit if enabled
        if (storageType == StorageType.MYSQL) {
            try {
                dataSource = databaseManager.initMySQLDataSource(messageManager, this);
                databaseManager.createTable(this, dataSource);
            } catch (SQLException e) {
                databaseManager.logSQLError(e);
            }
        }
        return true;
    }

    private int getVersion() {
        try {
            return Integer.parseInt(new BufferedReader(
                    new InputStreamReader(BookRules.class.getClassLoader().getResourceAsStream("version"))).lines().findFirst().get().replace(".", "")
            );
        } catch (Exception exception) {
            return 0;
        }
    }

}
