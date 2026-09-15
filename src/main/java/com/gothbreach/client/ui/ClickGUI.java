package com.gothbreach.client.ui;

import com.gothbreach.client.CheatClient;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.module.Module;
import com.gothbreach.client.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickGUI extends Screen {
    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule = null;
    private int scrollOffset = 0;
    private int settingsScroll = 0;
    private Module bindingModule = null; // модуль, для которого ждём нажатия клавиши

    private final int rowHeight = 18;
    private final int headerHeight = 22;
    private final int categoryPanelWidth = 90;
    private final int modulePanelWidth = 140;
    private final int settingsPanelWidth = 220;

    public ClickGUI() {
        super(Text.literal("Gothbreach Client"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x80000000);

        context.fill(10, 10, 10 + categoryPanelWidth + modulePanelWidth + settingsPanelWidth, 32, 0xFF202020);
        context.drawTextWithShadow(textRenderer, "Gothbreach Client", 16, 17, 0xFFFFFF);

        int catY = 38;
        int moduleY = 38;
        int settingsY = 38;
        int settingsX = 10 + categoryPanelWidth + modulePanelWidth;

        // Категории
        for (Category cat : Category.values()) {
            boolean selected = cat == selectedCategory;
            context.fill(10, catY, 10 + categoryPanelWidth, catY + rowHeight, selected ? 0xFF00AA00 : 0xFF303030);
            context.drawTextWithShadow(textRenderer, cat.getName(), 16, catY + 5, 0xFFFFFF);
            catY += rowHeight;
        }

        // Модули
        List<Module> modules = new ArrayList<>(CheatClient.moduleManager.getModulesInCategory(selectedCategory));
        int maxVisible = (height - 50) / rowHeight;
        int start = Math.min(scrollOffset, Math.max(0, modules.size() - maxVisible));
        int end = Math.min(start + maxVisible, modules.size());

        for (int i = start; i < end; i++) {
            Module mod = modules.get(i);
            boolean enabled = mod.isEnabled();
            boolean hovered = mouseX >= 10 + categoryPanelWidth && mouseX <= 10 + categoryPanelWidth + modulePanelWidth
                    && mouseY >= moduleY && mouseY < moduleY + rowHeight;

            int bg = enabled ? 0xFF005500 : (hovered ? 0xFF505050 : 0xFF303030);
            if (mod == selectedModule) bg = 0xFF5050AA;

            context.fill(10 + categoryPanelWidth, moduleY, 10 + categoryPanelWidth + modulePanelWidth, moduleY + rowHeight, bg);
            context.drawTextWithShadow(textRenderer, mod.getName(), 16 + categoryPanelWidth, moduleY + 5,
                    enabled ? 0x00FF00 : 0xFFFFFF);

            // Показываем бинд справа
            String bindText = mod.getKey() == -1 ? "" : GLFW.glfwGetKeyName(mod.getKey(), 0);
            if (bindText != null && !bindText.isEmpty()) {
                context.drawTextWithShadow(textRenderer, "[" + bindText.toUpperCase() + "]",
                        10 + categoryPanelWidth + modulePanelWidth - 40, moduleY + 5, 0xFFFF00);
            }
            moduleY += rowHeight;
        }

        // Настройки
        if (selectedModule != null) {
            context.fill(settingsX, settingsY, settingsX + settingsPanelWidth, height - 10, 0xFF181818);
            context.drawTextWithShadow(textRenderer, selectedModule.getName(), settingsX + 6, settingsY + 5, 0xFFFF00);
            settingsY += headerHeight + 4;

            // Кнопка бинда
            int bindBg = (bindingModule == selectedModule) ? 0xFFAA5500 : 0xFF282828;
            context.fill(settingsX + 4, settingsY, settingsX + settingsPanelWidth - 4, settingsY + rowHeight, bindBg);
            context.drawTextWithShadow(textRenderer, "Бинд", settingsX + 8, settingsY + 5, 0xFFFFFF);
            String keyName = selectedModule.getKey() == -1 ? "Нет" :
                    (GLFW.glfwGetKeyName(selectedModule.getKey(), 0) == null ? "?" :
                    GLFW.glfwGetKeyName(selectedModule.getKey(), 0).toUpperCase());
            context.drawTextWithShadow(textRenderer, keyName, settingsX + settingsPanelWidth - 70, settingsY + 5, 0x00FFFF);
            settingsY += rowHeight + 4;

            // Остальные настройки
            List<Field> settingFields = getSettingFields(selectedModule);
            int maxSettings = (height - 110) / rowHeight;
            int sStart = Math.min(settingsScroll, Math.max(0, settingFields.size() - maxSettings));
            int sEnd = Math.min(sStart + maxSettings, settingFields.size());

            for (int i = sStart; i < sEnd; i++) {
                Field field = settingFields.get(i);
                Setting setting = field.getAnnotation(Setting.class);
                try {
                    field.setAccessible(true);
                    Object value = field.get(selectedModule);
                    String label = setting.name().isEmpty() ? field.getName() : setting.name();

                    context.fill(settingsX + 4, settingsY, settingsX + settingsPanelWidth - 4, settingsY + rowHeight, 0xFF282828);
                    context.drawTextWithShadow(textRenderer, label, settingsX + 8, settingsY + 5, 0xFFFFFF);

                    String display = formatValue(value, setting);
                    context.drawTextWithShadow(textRenderer, display, settingsX + settingsPanelWidth - 70, settingsY + 5, 0x00FFFF);

                    if (value instanceof Number) {
                        double num = ((Number) value).doubleValue();
                        double min = setting.min();
                        double max = setting.max();
                        if (max > min) {
                            double norm = (num - min) / (max - min);
                            int sliderX = settingsX + 8;
                            int sliderW = settingsPanelWidth - 16;
                            int sliderY = settingsY + rowHeight - 4;
                            context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 2, 0xFF404040);
                            context.fill(sliderX, sliderY, sliderX + (int)(norm * sliderW), sliderY + 2, 0xFF00AA00);
                        }
                    }
                    settingsY += rowHeight;
                } catch (IllegalAccessException ignored) {}
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private String formatValue(Object value, Setting setting) {
        if (value instanceof Boolean b) return b ? "ON" : "OFF";
        if (value instanceof Number n) return String.format("%." + setting.decimalPlaces() + "f", n.doubleValue());
        return value.toString();
    }

    private List<Field> getSettingFields(Module mod) {
        List<Field> list = new ArrayList<>();
        for (Field f : mod.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Setting.class)) list.add(f);
        }
        return list;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Категории
        int catY = 38;
        for (Category cat : Category.values()) {
            if (mouseX >= 10 && mouseX <= 10 + categoryPanelWidth && mouseY >= catY && mouseY < catY + rowHeight) {
                selectedCategory = cat;
                selectedModule = null;
                scrollOffset = 0;
                return true;
            }
            catY += rowHeight;
        }

        // Модули
        List<Module> modules = new ArrayList<>(CheatClient.moduleManager.getModulesInCategory(selectedCategory));
        int moduleY = 38;
        int maxVisible = (height - 50) / rowHeight;
        int start = Math.min(scrollOffset, Math.max(0, modules.size() - maxVisible));
        int end = Math.min(start + maxVisible, modules.size());

        for (int i = start; i < end; i++) {
            if (mouseX >= 10 + categoryPanelWidth && mouseX <= 10 + categoryPanelWidth + modulePanelWidth
                    && mouseY >= moduleY && mouseY < moduleY + rowHeight) {
                Module mod = modules.get(i);
                if (button == 0) mod.toggle();
                else if (button == 1) {
                    selectedModule = (selectedModule == mod) ? null : mod;
                    settingsScroll = 0;
                    bindingModule = null;
                }
                return true;
            }
            moduleY += rowHeight;
        }

        // Настройки
        if (selectedModule != null) {
            int settingsX = 10 + categoryPanelWidth + modulePanelWidth;
            int settingsY = 38 + headerHeight + 4;

            // Кнопка бинда
            if (mouseX >= settingsX + 4 && mouseX <= settingsX + settingsPanelWidth - 4
                    && mouseY >= settingsY && mouseY < settingsY + rowHeight) {
                bindingModule = (bindingModule == selectedModule) ? null : selectedModule;
                return true;
            }
            settingsY += rowHeight + 4;

            List<Field> settingFields = getSettingFields(selectedModule);
            int maxSettings = (height - 110) / rowHeight;
            int sStart = Math.min(settingsScroll, Math.max(0, settingFields.size() - maxSettings));
            int sEnd = Math.min(sStart + maxSettings, settingFields.size());

            for (int i = sStart; i < sEnd; i++) {
                Field field = settingFields.get(i);
                if (mouseX >= settingsX + 4 && mouseX <= settingsX + settingsPanelWidth - 4
                        && mouseY >= settingsY && mouseY < settingsY + rowHeight) {
                    Setting setting = field.getAnnotation(Setting.class);
                    try {
                        field.setAccessible(true);
                        Object value = field.get(selectedModule);

                        if (value instanceof Boolean) {
                            field.setBoolean(selectedModule, !(Boolean) value);
                        } else if (value instanceof Number) {
                            int sliderX = settingsX + 8;
                            int sliderW = settingsPanelWidth - 16;
                            double norm = (mouseX - sliderX) / (double) sliderW;
                            norm = Math.max(0, Math.min(1, norm));
                            double newVal = setting.min() + norm * (setting.max() - setting.min());
                            if (value instanceof Integer) field.setInt(selectedModule, (int) newVal);
                            else if (value instanceof Double) field.setDouble(selectedModule, newVal);
                            else if (value instanceof Float) field.setFloat(selectedModule, (float) newVal);
                        } else if (value instanceof String) {
                            String[] options = setting.values();
                            if (options.length > 0) {
                                int idx = Arrays.asList(options).indexOf(value);
                                idx = (idx + 1) % options.length;
                                field.set(selectedModule, options[idx]);
                            }
                        }
                        selectedModule.saveConfig();
                    } catch (IllegalAccessException ignored) {}
                    return true;
                }
                settingsY += rowHeight;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Если ждём бинд — сохраняем клавишу
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKey(-1); // сброс бинда
            } else {
                bindingModule.setKey(keyCode);
            }
            bindingModule = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedModule != null && mouseX > 10 + categoryPanelWidth + modulePanelWidth) {
            settingsScroll = Math.max(0, settingsScroll - (int) verticalAmount);
            return true;
        }
        scrollOffset = Math.max(0, scrollOffset - (int) verticalAmount);
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}