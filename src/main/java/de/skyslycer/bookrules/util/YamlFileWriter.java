package de.skyslycer.bookrules.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class YamlFileWriter {
    private File file;
    private File directory;
    private YamlConfiguration yamlConfiguration;
    private boolean successful;

    public YamlFileWriter(String filePath, String fileName) {
        successful = true;
        this.file = new File(filePath, fileName);
        this.directory = new File(filePath);
        try {
            directory.mkdir();
            file.createNewFile();
            this.yamlConfiguration = new YamlConfiguration();
            this.yamlConfiguration.load(this.file);
        }catch (InvalidConfigurationException | IOException exception) {
            exception.printStackTrace();
            successful = false;
        }
    }

    public boolean isSuccessful() {
        return successful;
    }

    public YamlFileWriter setValue(String valuePath, Object value) {
        yamlConfiguration.set(valuePath, value);
        return this;
    }

    public String getString(String valuePath) {
        return yamlConfiguration.getString(valuePath);
    }

    public String getPath() {
        return yamlConfiguration.getCurrentPath();
    }

    public boolean getBoolean(String valuePath) {
        if(yamlConfiguration.getString(valuePath) == null) {
            return false;
        }
        if(yamlConfiguration.getString(valuePath).equals("true")) {
            return true;
        }else return false;
    }

    public int getInt(String valuePath) {
        return yamlConfiguration.getInt(valuePath);
    }

    public boolean exist() {
        return file.exists();
    }

    public boolean contains(String valuePath) {
        return yamlConfiguration.contains(valuePath);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getArrayList(String valuePath) {
        return (ArrayList<String>) yamlConfiguration.getList(valuePath);
    }

    public HashMap<String, String> getHashMap(String valuePath) {
        HashMap<String, String> getMap = new HashMap<>();
        for (String key : yamlConfiguration.getConfigurationSection(valuePath).getKeys(false)) {
            ((Map<String, String>) getMap).put(key, (String) yamlConfiguration.get(valuePath + "." + key));
        }

        return getMap;
    }

    public Set<String> getKeys(String valuePath) {
        return yamlConfiguration.getConfigurationSection(valuePath).getKeys(false);
    }

    public YamlFileWriter save() {
        try {
            this.yamlConfiguration.save(this.file);
        }catch(IOException e) {
            e.printStackTrace();
        }
        return this;
    }
}
