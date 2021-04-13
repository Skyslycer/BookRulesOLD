package de.skyslycer.bookrules.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class YamlFileWriter {
    private File f;
    private YamlConfiguration c;

    public YamlFileWriter(String filePath, String fileName) {
        this.f = new File(filePath, fileName);
        this.c = YamlConfiguration.loadConfiguration(this.f);
    }

    public YamlFileWriter setValue(String valuePath, Object Value) {
        c.set(valuePath, Value);
        return this;
    }

    public String getString(String valuePath) {
        return c.getString(valuePath);
    }

    public String getPath() {
        return c.getCurrentPath();
    }

    public boolean getBoolean(String valuePath) {
        if(c.getString(valuePath).equals("true")) {
            return true;
        }else return false;
    }

    public int getInt(String valuePath) {
        return c.getInt(valuePath);
    }

    public int getFloat(String valuePath) {
        return c.getInt(valuePath);
    }

    public boolean exist() {
        return f.exists();
    }

    public boolean contains(String valuePath) {
        return c.contains(valuePath);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getArrayList(String valuePath) {
        return (ArrayList<String>) c.getList(valuePath);
    }

    public HashMap<String, String> getHashMap(String valuePath) {
        HashMap<String, String> getMap = new HashMap<>();
        for (String key : c.getConfigurationSection(valuePath).getKeys(false)) {
            ((Map<String, String>) getMap).put(key, (String) c.get(valuePath + "." + key));
        }

        return getMap;
    }

    public Set<String> getKeys(String valuePath) {
        return c.getConfigurationSection(valuePath).getKeys(false);
    }

    public YamlFileWriter save() {
        try {
            this.c.save(this.f);
        }catch(IOException e) {
        }
        return this;
    }
}
