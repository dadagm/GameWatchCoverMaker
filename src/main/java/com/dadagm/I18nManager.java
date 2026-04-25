package com.dadagm;

import java.util.Locale;
import java.util.ResourceBundle;
import java.io.*;
import java.util.Properties;

public class I18nManager {
    private static I18nManager instance;
    private ResourceBundle messages;
    private Locale currentLocale;
    
    private static final String APP_SUPPORT_DIR = System.getProperty("user.home") + "/Library/Application Support/GameWatchCoverMaker";
    private static final String CONFIG_FILE = APP_SUPPORT_DIR + "/config.properties";
    private static final String LANGUAGE_KEY = "language";
    
    private I18nManager() {
        File dir = new File(APP_SUPPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        currentLocale = loadSavedLanguage();
        
        Locale targetLocale = currentLocale;
        if (currentLocale.equals(Locale.ENGLISH)) {
            targetLocale = Locale.ROOT;
        }
        
        messages = ResourceBundle.getBundle("messages", targetLocale, I18nManager.class.getClassLoader());
    }
    
    public static synchronized I18nManager getInstance() {
        if (instance == null) {
            instance = new I18nManager();
        }
        return instance;
    }
    
    private Locale loadSavedLanguage() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            String language = prop.getProperty(LANGUAGE_KEY);
            if (language != null) {
                if (language.equals("zh_CN")) {
                    return Locale.CHINA;
                } else if (language.equals("en")) {
                    return Locale.ENGLISH;
                } else {
                    String[] parts = language.split("_");
                    if (parts.length == 2) {
                        return new Locale(parts[0], parts[1]);
                    } else {
                        return new Locale(language);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Locale.ENGLISH;
    }
    
    private void saveLanguage(Locale locale) {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            if (locale.equals(Locale.CHINA)) {
                prop.setProperty(LANGUAGE_KEY, "zh_CN");
            } else if (locale.equals(Locale.ENGLISH)) {
                prop.setProperty(LANGUAGE_KEY, "en");
            } else {
                prop.setProperty(LANGUAGE_KEY, locale.toString());
            }
            prop.store(output, "Game&Watch Cover Maker Config");
        } catch (IOException e) {
            System.err.println("Failed to save language setting: " + e.getMessage());
        }
    }
    
    public void switchLanguage(Locale locale) {
        System.out.println("switch language to: " + locale);
        currentLocale = locale;
        
        saveLanguage(locale);
        
        ResourceBundle.clearCache(I18nManager.class.getClassLoader());
        
        Locale targetLocale = locale;
        if (locale.equals(Locale.ENGLISH)) {
            targetLocale = Locale.ROOT;
        }
        
        messages = ResourceBundle.getBundle("messages", targetLocale, I18nManager.class.getClassLoader());
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public String getString(String key) {
        return messages.getString(key);
    }
    
    public ResourceBundle getMessages() {
        return messages;
    }
}