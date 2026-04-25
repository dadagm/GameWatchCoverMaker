package com.dadagm;
public class ConfigManager {
    private static ConfigManager instance;
    private final String VERSION = "1.2";
    
    private ConfigManager() {
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    public String getVersion() {
        return VERSION;
    }
}