package de.skyslycer.bookrules.api;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.StorageType;
import org.bukkit.Bukkit;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class RulesAPI {
    BookRules bookRules;

    public CompletableFuture<Boolean> playerHasAcceptedRules(String uuid) {
        if (bookRules == null) {
            this.bookRules = BookRules.getAPIData();
        }

        if (bookRules.storageType == StorageType.MYSQL) {
            return CompletableFuture.supplyAsync(() -> {
                try (Connection connection = bookRules.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                        "SELECT player_uuid FROM " + bookRules.databaseManager.databasePrefix + "players WHERE player_uuid = ?;")) {
                    statement.setString(1, uuid);
                    ResultSet resultSet = statement.executeQuery();
                    return resultSet.first();
                } catch (SQLException e) {
                    bookRules.databaseManager.logSQLError(e);
                }
                return false;
            });
        } else return CompletableFuture.completedFuture(bookRules.players.contains(uuid));
    }

    public void acceptRules(String uuid) {
        if (bookRules == null) {
            bookRules = BookRules.getAPIData();
        }

        if (bookRules.storageType == StorageType.MYSQL) {
            Bukkit.getScheduler().runTaskAsynchronously(bookRules, () -> {
                try (PreparedStatement statement = bookRules.dataSource.getConnection().prepareStatement(
                        "INSERT IGNORE INTO " + bookRules.databaseManager.databasePrefix + "players(player_uuid) VALUES(?);")) {
                    statement.setString(1, uuid);
                    statement.execute();
                } catch (SQLException e) {
                    bookRules.databaseManager.logSQLError(e);
                }
            });
        } else bookRules.players.add(uuid);
    }

    public void declineRules(String uuid) {
        if (bookRules == null) {
            bookRules = BookRules.getAPIData();
        }

        if (bookRules.storageType == StorageType.MYSQL) {
            Bukkit.getScheduler().runTaskAsynchronously(bookRules, () -> {
                try (PreparedStatement statement = bookRules.dataSource.getConnection().prepareStatement(
                        "DELETE FROM " + bookRules.databaseManager.databasePrefix + "players WHERE player_uuid = ?;")) {
                    statement.setString(1, uuid);
                    statement.execute();
                } catch (SQLException e) {
                    bookRules.databaseManager.logSQLError(e);
                }
            });
        } else bookRules.players.remove(uuid);
    }

    public void getPlayerData() {
        if (bookRules == null) {
            bookRules = BookRules.getAPIData();
        }

        bookRules.players.clear();
        Bukkit.getScheduler().runTaskAsynchronously(bookRules, () -> {
            if (bookRules.storageType == StorageType.LOCAL) {
                try {
                    File file = new File("plugins//BookRules//players.txt");
                    file.createNewFile();
                    readLocalDB();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void readLocalDB() throws IOException {
        FileReader reader = new FileReader("plugins//BookRules//players.txt");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            bookRules.players.add(line);
            bookRules.messageManager.sendDebug("Adding UUID " + line + " to cache (accepted the rules)");
        }

        reader.close();
    }

    public void setPlayerData() {
        if (bookRules == null) {
            bookRules = BookRules.getAPIData();
        }

        if (bookRules.storageType == StorageType.LOCAL) {
            try {
                File file = new File("plugins//BookRules//players.txt");
                file.delete();
                file.createNewFile();
                FileWriter writer = new FileWriter("plugins//BookRules//players.txt", false);
                String toWrite = String.join("\n", bookRules.players);
                writer.write(toWrite);

                if (!(toWrite.length() == 0)) {
                    bookRules.messageManager.sendDebug("Saving cache to file:\n" + toWrite);
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadPlayerData() {
        if (bookRules == null) {
            bookRules = BookRules.getAPIData();
        }

        Bukkit.getScheduler().runTaskAsynchronously(bookRules, () -> {
            if (bookRules.storageType == StorageType.LOCAL) {
                try {
                    File file = new File("plugins//BookRules//players.txt");
                    file.delete();
                    file.createNewFile();
                    FileWriter writer = new FileWriter("plugins//BookRules//players.txt", false);
                    String toWrite = String.join("\n", bookRules.players);
                    writer.write(toWrite);

                    if (!(toWrite.length() == 0)) {
                        bookRules.messageManager.sendDebug("Saving cache to file:\n" + toWrite);
                    }

                    writer.close();
                    bookRules.players.clear();
                    readLocalDB();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
