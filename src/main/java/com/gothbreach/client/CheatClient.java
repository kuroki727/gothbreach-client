package com.gothbreach.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;
import com.gothbreach.client.module.ModuleManager;
import com.gothbreach.client.event.EventManager;
import com.gothbreach.client.ui.ClickGUI;

public class CheatClient implements ClientModInitializer {
    public static final String MOD_ID = "cheat-client";
    public static MinecraftClient mc;
    public static ModuleManager moduleManager;
    public static EventManager eventManager;
    public static ClickGUI clickGUI;
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        System.out.println("[Gothbreach] Инициализация клиента...");
        mc = MinecraftClient.getInstance();
        eventManager = new EventManager();
        moduleManager = new ModuleManager();
        clickGUI = new ClickGUI();
        moduleManager.init();
        moduleManager.loadConfigs();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cheat-client.opengui",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "Gothbreach Client"
        ));

        // Отложенное открытие GUI (безопасно)
        mc.execute(() -> {
            if (mc.currentScreen == null) {
                mc.setScreen(clickGUI);
                System.out.println("[Gothbreach] GUI открыт.");
            }
        });
        System.out.println("[Gothbreach] Инициализация завершена.");
    }
}
