package com.gothbreach.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gothbreach.client.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled = false;
    private int key = 0;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Path.of("gothbreach_config");

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void toggle() { if (enabled) disable(); else enable(); }
    public void enable() { enabled = true; onEnable(); }
    public void disable() { enabled = false; onDisable(); }
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender(MatrixStack matrices, float tickDelta) {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }

    public void saveConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            File file = CONFIG_DIR.resolve(name + ".json").toFile();
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        File file = CONFIG_DIR.resolve(name + ".json").toFile();
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Module loaded = GSON.fromJson(reader, this.getClass());
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Setting.class)) {
                        try {
                            field.setAccessible(true);
                            field.set(this, field.get(loaded));
                        } catch (IllegalAccessException ignored) {}
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
