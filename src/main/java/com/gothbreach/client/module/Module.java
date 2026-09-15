package com.gothbreach.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
    private int key = -1; // -1 = нет бинда
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Path.of("gothbreach_config");
    private static final Path MODULES_DIR = CONFIG_DIR.resolve("modules");

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void toggle() { if (enabled) disable(); else enable(); }

    public void enable() {
        enabled = true;
        onEnable();
        saveConfig();
    }

    public void disable() {
        enabled = false;
        onDisable();
        saveConfig();
    }

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
    public void setKey(int key) { this.key = key; saveConfig(); }

    // ===== Сохранение конфига =====
    public void saveConfig() {
        try {
            Files.createDirectories(MODULES_DIR);
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            json.addProperty("key", key);

            for (Field field : this.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Setting.class)) continue;
                field.setAccessible(true);
                Object value = field.get(this);
                if (value instanceof Boolean b) json.addProperty(field.getName(), b);
                else if (value instanceof Integer i) json.addProperty(field.getName(), i);
                else if (value instanceof Double d) json.addProperty(field.getName(), d);
                else if (value instanceof Float f) json.addProperty(field.getName(), f);
                else if (value instanceof Long l) json.addProperty(field.getName(), l);
                else if (value instanceof String s) json.addProperty(field.getName(), s);
            }

            File file = MODULES_DIR.resolve(name + ".json").toFile();
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        File file = MODULES_DIR.resolve(name + ".json").toFile();
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) return;

            if (json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
            if (json.has("key")) key = json.get("key").getAsInt();

            for (Field field : this.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Setting.class)) continue;
                if (!json.has(field.getName())) continue;
                field.setAccessible(true);

                var element = json.get(field.getName());
                Class<?> type = field.getType();
                if (type == boolean.class) field.setBoolean(this, element.getAsBoolean());
                else if (type == int.class) field.setInt(this, element.getAsInt());
                else if (type == double.class) field.setDouble(this, element.getAsDouble());
                else if (type == float.class) field.setFloat(this, element.getAsFloat());
                else if (type == long.class) field.setLong(this, element.getAsLong());
                else if (type == String.class) field.set(this, element.getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}