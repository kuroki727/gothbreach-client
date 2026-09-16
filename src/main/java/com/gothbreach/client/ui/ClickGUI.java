package com.gothbreach.client.ui;

import com.gothbreach.client.CheatClient;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.modules.client.ClickGUIModule;
import com.gothbreach.client.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.*;

public class ClickGUI extends Screen {
    private static final float MIN_SCALE = 0.3f;
    private static final float MAX_SCALE = 1.5f;
    private static final float SCALE_STEP = 0.05f;

    private static final int PANEL_WIDTH = 130;
    private static final int MODULE_ROW = 14;
    private static final int HEADER_HEIGHT = 15;
    private static final int SETTING_ROW = 14;
    private static final int PANEL_GAP = 4;

    private static final int BG_DIM = 0x80000000;
    private static final int HEADER_BG = 0xF02A2A2A;
    private static final int ROW_BG = 0xFF1E1E1E;
    private static final int ROW_HOVER = 0xFF252525;
    private static final int ROW_ENABLED = 0xFF2A2A2A;
    private static final int ROW_SELECTED = 0xFF3A3A3A;
    private static final int ROW_SETTINGS_ONLY = 0xFF252535;
    private static final int TEXT = 0xFFD0D0D0;
    private static final int TEXT_DIM = 0xFF707070;
    private static final int ACCENT = 0xFF3B82F6;
    private static final int SLIDER_BG = 0xFF151515;
    private static final int GREEN = 0xFF10B981;
    private static final int RED = 0xFFEF4444;
    private static final int SEARCH_BG = 0xFF1E1E1E;
    private static final int SEARCH_BORDER = 0xFF3B82F6;
    private static final int MODAL_BG = 0xF0252525;
    private static final int MODAL_BORDER = 0xFF3B82F6;
    private static final int SETTING_BG = 0xFF161616;
    private static final int SETTING_BG_HOVER = 0xFF202020;

    private final Map<Category, Panel> panels = new LinkedHashMap<>();
    private Panel draggingPanel = null;
    private int dragDX, dragDY;

    private Module selectedModule = null;
    private Module bindingModule = null;

    private String searchQuery = "";
    private boolean searchFocused = false;

    private Module inputModule = null;
    private Field inputField = null;
    private String inputText = "";
    private String inputTitle = "";

    private static class Panel {
        Category category;
        int x, y;
        boolean collapsed;
        Panel(Category c, int x, int y) {
            this.category = c;
            this.x = x;
            this.y = y;
        }
    }

    public ClickGUI() {
        super(Text.literal("Gothbreach"));
        int x = 10;
        for (Category cat : Category.values()) {
            panels.put(cat, new Panel(cat, x, 15));
            x += PANEL_WIDTH + PANEL_GAP;
        }
    }

    private ClickGUIModule getConfig() {
        if (CheatClient.moduleManager == null) return null;
        Module m = CheatClient.moduleManager.get("ClickGUI");
        return (m instanceof ClickGUIModule guiModule) ? guiModule : null;
    }

    /**
     * Вычисляем активный масштаб.
     * Если включён autoScale — подгоняем так, чтобы все панели влезли в экран.
     * Иначе — берём из настроек модуля.
     */
    private float getActiveScale() {
        ClickGUIModule cfg = getConfig();
        if (cfg == null) return 0.75f;

        if (cfg.autoScale) {
            float totalNeeded = Category.values().length * (PANEL_WIDTH + PANEL_GAP) + 20;
            float auto = this.width / totalNeeded;
            return Math.max(MIN_SCALE, Math.min(MAX_SCALE, auto));
        }
        return (float) Math.max(MIN_SCALE, Math.min(MAX_SCALE, cfg.guiScale));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float scale = getActiveScale();

        ctx.fill(0, 0, width, height, BG_DIM);

        if (inputModule != null && inputField != null) {
            ctx.fill(0, 0, width, height, 0xD0000000);
            ctx.getMatrices().push();
            ctx.getMatrices().scale(scale, scale, 1.0f);
            int smx = (int) (mouseX / scale);
            int smy = (int) (mouseY / scale);
            renderInputModal(ctx, smx, smy);
            ctx.getMatrices().pop();
            return;
        }

        ctx.getMatrices().push();
        ctx.getMatrices().scale(scale, scale, 1.0f);

        int smx = (int) (mouseX / scale);
        int smy = (int) (mouseY / scale);

        renderSearch(ctx, scale);
        for (Panel panel : panels.values()) {
            renderPanel(ctx, panel, smx, smy);
        }

        if (isCtrlPressed()) {
            renderScaleIndicator(ctx, scale);
        }

        ctx.getMatrices().pop();
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderScaleIndicator(DrawContext ctx, float scale) {
        String text = "Scale: " + String.format("%.2f", scale);
        int scaledW = (int) (this.width / scale);
        int tw = textRenderer.getWidth(text);
        int x = scaledW - tw - 10;
        int y = 10;
        ctx.fill(x - 4, y - 3, x + tw + 4, y + 11, 0xFF1A1A1A);
        ctx.fill(x - 4, y - 3, x + tw + 4, y - 2, ACCENT);
        ctx.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFFFF);
    }

    private boolean isCtrlPressed() {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)
            || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private void renderSearch(DrawContext ctx, float scale) {
        int searchW = 140;
        int searchH = 14;
        int searchX = (int) ((this.width / scale) - searchW) / 2;
        int searchY = 3;

        ctx.fill(searchX, searchY, searchX + searchW, searchY + searchH, SEARCH_BG);

        if (searchFocused) {
            ctx.fill(searchX, searchY, searchX + searchW, searchY + 1, SEARCH_BORDER);
            ctx.fill(searchX, searchY + searchH - 1, searchX + searchW, searchY + searchH, SEARCH_BORDER);
            ctx.fill(searchX, searchY, searchX + 1, searchY + searchH, SEARCH_BORDER);
            ctx.fill(searchX + searchW - 1, searchY, searchX + searchW, searchY + searchH, SEARCH_BORDER);
        }

        String display;
        int color;
        if (searchQuery.isEmpty() && !searchFocused) {
            display = "Search...";
            color = TEXT_DIM;
        } else {
            display = searchQuery + (searchFocused ? "_" : "");
            color = 0xFFFFFFFF;
        }
        ctx.drawTextWithShadow(textRenderer, display, searchX + 4, searchY + 3, color);
    }

    private void renderPanel(DrawContext ctx, Panel panel, int mouseX, int mouseY) {
        ctx.fill(panel.x, panel.y, panel.x + PANEL_WIDTH, panel.y + HEADER_HEIGHT, HEADER_BG);
        ctx.drawTextWithShadow(textRenderer, panel.category.getName(), panel.x + 4, panel.y + 4, 0xFFFFFFFF);

        String indicator = panel.collapsed ? "+" : "-";
        int indW = textRenderer.getWidth(indicator);
        ctx.drawTextWithShadow(textRenderer, indicator, panel.x + PANEL_WIDTH - indW - 4, panel.y + 4, TEXT_DIM);

        if (panel.collapsed) return;

        List<Module> modules = getFilteredModules(panel.category);
        int y = panel.y + HEADER_HEIGHT;

        for (Module mod : modules) {
            boolean settingsOnly = mod.isSettingsOnly();
            boolean enabled = !settingsOnly && mod.isEnabled();
            boolean selected = mod == selectedModule;
            boolean hovered = mouseX >= panel.x && mouseX <= panel.x + PANEL_WIDTH
                    && mouseY >= y && mouseY < y + MODULE_ROW;

            int bg;
            if (selected) bg = ROW_SELECTED;
            else if (settingsOnly && hovered) bg = ROW_HOVER;
            else if (settingsOnly) bg = ROW_SETTINGS_ONLY;
            else if (enabled) bg = ROW_ENABLED;
            else if (hovered) bg = ROW_HOVER;
            else bg = ROW_BG;

            ctx.fill(panel.x, y, panel.x + PANEL_WIDTH, y + MODULE_ROW, bg);

            if (enabled) {
                ctx.fill(panel.x, y, panel.x + 2, y + MODULE_ROW, ACCENT);
            }
            if (settingsOnly) {
                ctx.fill(panel.x, y, panel.x + 2, y + MODULE_ROW, 0xFF8B5CF6);
            }

            int textColor = enabled ? 0xFFFFFFFF : (settingsOnly ? 0xFFB0A0E0 : TEXT);
            String modName = mod.getName();
            int maxNameW = PANEL_WIDTH - 12;
            if (mod.getKey() != -1) maxNameW -= 30;
            modName = truncate(modName, maxNameW);

            ctx.drawTextWithShadow(textRenderer, modName, panel.x + 5, y + 3, textColor);

            // Для settings-only модулей справа рисуем шестерёнку
            if (settingsOnly) {
                ctx.drawTextWithShadow(textRenderer, ">", panel.x + PANEL_WIDTH - 8, y + 3, TEXT_DIM);
            } else if (mod.getKey() != -1) {
                String keyName = getKeyName(mod.getKey());
                if (keyName != null) {
                    int kw = textRenderer.getWidth(keyName);
                    ctx.drawTextWithShadow(textRenderer, keyName, panel.x + PANEL_WIDTH - kw - 4, y + 3, TEXT_DIM);
                }
            }

            y += MODULE_ROW;

            if (selected) {
                y = renderSettings(ctx, mod, panel.x, y, mouseX, mouseY);
            }
        }
    }

    private int renderSettings(DrawContext ctx, Module mod, int panelX, int y, int mouseX, int mouseY) {
        // Bind только для обычных модулей
        if (!mod.isSettingsOnly()) {
            y = renderBindRow(ctx, mod, panelX, y, mouseX, mouseY);
        }

        for (Field field : getSettingFields(mod)) {
            Setting s = field.getAnnotation(Setting.class);
            try {
                field.setAccessible(true);
                Object value = field.get(mod);
                String name = s.name().isEmpty() ? field.getName() : s.name();

                boolean hovered = mouseX >= panelX + 2 && mouseX <= panelX + PANEL_WIDTH - 2
                        && mouseY >= y && mouseY < y + SETTING_ROW;

                ctx.fill(panelX + 2, y, panelX + PANEL_WIDTH - 2, y + SETTING_ROW,
                        hovered ? SETTING_BG_HOVER : SETTING_BG);

                String valueStr = "";
                int valueColor = ACCENT;

                if (value instanceof Boolean b) {
                    valueStr = b ? "ON" : "OFF";
                    valueColor = b ? GREEN : RED;
                } else if (value instanceof Number n) {
                    valueStr = formatNumber(n, s);
                    int w = textRenderer.getWidth(valueStr);
                    int txtX = panelX + PANEL_WIDTH - w - 5;
                    if (mouseX >= txtX - 2 && mouseX <= panelX + PANEL_WIDTH - 3) {
                        valueColor = 0xFFFFAA00;
                    }
                } else if (value instanceof String str) {
                    valueStr = str;
                }

                int valueW = textRenderer.getWidth(valueStr);
                int nameMaxW = PANEL_WIDTH - 12 - valueW - 4;
                String nameStr = truncate(name, nameMaxW);
                ctx.drawTextWithShadow(textRenderer, nameStr, panelX + 5, y + 3, TEXT);

                if (!valueStr.isEmpty()) {
                    int valX = panelX + PANEL_WIDTH - valueW - 5;
                    ctx.drawTextWithShadow(textRenderer, valueStr, valX, y + 3, valueColor);
                }

                if (value instanceof Number n && s.max() > s.min()) {
                    double num = n.doubleValue();
                    double norm = Math.max(0, Math.min(1, (num - s.min()) / (s.max() - s.min())));
                    int sliderY = y + SETTING_ROW - 3;
                    int sliderX = panelX + 3;
                    int sliderW = PANEL_WIDTH - 6;
                    ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 2, SLIDER_BG);
                    ctx.fill(sliderX, sliderY, sliderX + (int)(norm * sliderW), sliderY + 2, ACCENT);
                }

                y += SETTING_ROW;
            } catch (Exception ignored) {}
        }
        return y;
    }

    private int renderBindRow(DrawContext ctx, Module mod, int panelX, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= panelX + 2 && mouseX <= panelX + PANEL_WIDTH - 2
                && mouseY >= y && mouseY < y + SETTING_ROW;
        boolean isBinding = bindingModule == mod;

        int bg;
        if (isBinding) bg = 0xFF7A3A00;
        else if (hovered) bg = SETTING_BG_HOVER;
        else bg = SETTING_BG;

        ctx.fill(panelX + 2, y, panelX + PANEL_WIDTH - 2, y + SETTING_ROW, bg);
        ctx.drawTextWithShadow(textRenderer, "Bind", panelX + 5, y + 3, TEXT);

        String keyName = isBinding ? "..." :
                (mod.getKey() == -1 ? "None" : getKeyName(mod.getKey()));
        if (keyName == null) keyName = "None";
        int color = isBinding ? 0xFFFFAA00 : (mod.getKey() == -1 ? TEXT_DIM : ACCENT);
        int w = textRenderer.getWidth(keyName);
        ctx.drawTextWithShadow(textRenderer, keyName, panelX + PANEL_WIDTH - w - 5, y + 3, color);

        return y + SETTING_ROW;
    }

    private void renderInputModal(DrawContext ctx, int mouseX, int mouseY) {
        int scaledW = (int) (this.width / getActiveScale());
        int scaledH = (int) (this.height / getActiveScale());

        int modalW = 180;
        int modalH = 70;
        int modalX = (scaledW - modalW) / 2;
        int modalY = (scaledH - modalH) / 2;

        ctx.fill(0, 0, scaledW, scaledH, 0xB0000000);

        ctx.fill(modalX, modalY, modalX + modalW, modalY + modalH, MODAL_BG);
        ctx.fill(modalX, modalY, modalX + modalW, modalY + 1, MODAL_BORDER);
        ctx.fill(modalX, modalY + modalH - 1, modalX + modalW, modalY + modalH, MODAL_BORDER);
        ctx.fill(modalX, modalY, modalX + 1, modalY + modalH, MODAL_BORDER);
        ctx.fill(modalX + modalW - 1, modalY, modalX + modalW, modalY + modalH, MODAL_BORDER);

        ctx.drawTextWithShadow(textRenderer, inputTitle, modalX + 8, modalY + 8, 0xFFFFFFFF);

        int fieldX = modalX + 8;
        int fieldY = modalY + 28;
        int fieldW = modalW - 16;
        int fieldH = 18;
        ctx.fill(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF0D0D0D);
        ctx.fill(fieldX, fieldY, fieldX + fieldW, fieldY + 1, MODAL_BORDER);
        ctx.fill(fieldX, fieldY + fieldH - 1, fieldX + fieldW, fieldY + fieldH, MODAL_BORDER);
        ctx.fill(fieldX, fieldY, fieldX + 1, fieldY + fieldH, MODAL_BORDER);
        ctx.fill(fieldX + fieldW - 1, fieldY, fieldX + fieldW, fieldY + fieldH, MODAL_BORDER);

        String shown = inputText.isEmpty() ? "_" : inputText + "_";
        ctx.drawTextWithShadow(textRenderer, shown, fieldX + 5, fieldY + 5, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Enter - OK, Esc - Cancel", modalX + 8, modalY + modalH - 12, TEXT_DIM);
    }

    private String truncate(String text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisW = textRenderer.getWidth(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (textRenderer.getWidth(sb.toString() + c) + ellipsisW > maxWidth) break;
            sb.append(c);
        }
        return sb + ellipsis;
    }

    private String formatNumber(Number n, Setting s) {
        if (n instanceof Integer || n instanceof Long) return String.valueOf(n.intValue());
        if (n instanceof Float f) {
            if (f == f.intValue()) return String.valueOf(f.intValue());
        }
        return String.format("%." + s.decimalPlaces() + "f", n.doubleValue());
    }

    private String getKeyName(int key) {
        if (key <= 0) return null;
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BKSP";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            default -> "K" + key;
        };
    }

    private List<Module> getFilteredModules(Category cat) {
        List<Module> result = new ArrayList<>();
        String q = searchQuery.toLowerCase();
        for (Module m : CheatClient.moduleManager.getModulesInCategory(cat)) {
            if (q.isEmpty() || m.getName().toLowerCase().contains(q)) {
                result.add(m);
            }
        }
        return result;
    }

    private List<Field> getSettingFields(Module mod) {
        List<Field> list = new ArrayList<>();
        for (Field f : mod.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Setting.class)) list.add(f);
        }
        return list;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        float scale = getActiveScale();
        int mouseX = (int) (mx / scale);
        int mouseY = (int) (my / scale);

        if (inputModule != null && inputField != null) {
            cancelInput();
            return true;
        }

        int scaledW = (int) (this.width / scale);
        int searchW = 140;
        int searchX = (scaledW - searchW) / 2;
        if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= 3 && mouseY <= 17) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        List<Panel> list = new ArrayList<>(panels.values());
        Collections.reverse(list);

        for (Panel panel : list) {
            if (mouseX >= panel.x && mouseX <= panel.x + PANEL_WIDTH
                    && mouseY >= panel.y && mouseY < panel.y + HEADER_HEIGHT) {
                if (button == 0) {
                    draggingPanel = panel;
                    dragDX = mouseX - panel.x;
                    dragDY = mouseY - panel.y;
                } else if (button == 1) {
                    panel.collapsed = !panel.collapsed;
                }
                panels.remove(panel.category);
                panels.put(panel.category, panel);
                return true;
            }

            if (panel.collapsed) continue;

            List<Module> modules = getFilteredModules(panel.category);
            int y = panel.y + HEADER_HEIGHT;

            for (Module mod : modules) {
                if (mouseX >= panel.x && mouseX <= panel.x + PANEL_WIDTH
                        && mouseY >= y && mouseY < y + MODULE_ROW) {
                    if (mod.isSettingsOnly()) {
                        // Не переключаем, а открываем настройки
                        selectedModule = (selectedModule == mod) ? null : mod;
                        bindingModule = null;
                    } else if (button == 0) {
                        mod.toggle();
                    } else if (button == 1) {
                        selectedModule = (selectedModule == mod) ? null : mod;
                        bindingModule = null;
                    }
                    return true;
                }
                y += MODULE_ROW;

                if (mod == selectedModule) {
                    // Bind — только для обычных модулей
                    if (!mod.isSettingsOnly()) {
                        if (mouseX >= panel.x + 2 && mouseX <= panel.x + PANEL_WIDTH - 2
                                && mouseY >= y && mouseY < y + SETTING_ROW) {
                            bindingModule = (bindingModule == mod) ? null : mod;
                            return true;
                        }
                        y += SETTING_ROW;
                    }

                    for (Field field : getSettingFields(mod)) {
                        if (mouseX >= panel.x + 2 && mouseX <= panel.x + PANEL_WIDTH - 2
                                && mouseY >= y && mouseY < y + SETTING_ROW) {
                            Setting s = field.getAnnotation(Setting.class);
                            try {
                                field.setAccessible(true);
                                Object value = field.get(mod);

                                if (value instanceof Boolean) {
                                    field.setBoolean(mod, !(Boolean) value);
                                    mod.saveConfig();
                                } else if (value instanceof Number) {
                                    String txt = formatNumber((Number) value, s);
                                    int w = textRenderer.getWidth(txt);
                                    int txtX = panel.x + PANEL_WIDTH - w - 5;
                                    if (mouseX >= txtX - 2) {
                                        openInput(mod, field, (Number) value, s);
                                    } else {
                                        int sliderX = panel.x + 3;
                                        int sliderW = PANEL_WIDTH - 6;
                                        double norm = (mouseX - sliderX) / (double) sliderW;
                                        norm = Math.max(0, Math.min(1, norm));
                                        double newVal = s.min() + norm * (s.max() - s.min());
                                        if (value instanceof Integer) field.setInt(mod, (int) newVal);
                                        else if (value instanceof Double) field.setDouble(mod, newVal);
                                        else if (value instanceof Float) field.setFloat(mod, (float) newVal);
                                        else if (value instanceof Long) field.setLong(mod, (long) newVal);
                                        mod.saveConfig();
                                    }
                                } else if (value instanceof String) {
                                    String[] opts = s.values();
                                    if (opts.length > 0) {
                                        int idx = Arrays.asList(opts).indexOf(value);
                                        idx = (idx + 1) % opts.length;
                                        field.set(mod, opts[idx]);
                                        mod.saveConfig();
                                    }
                                }
                            } catch (Exception ignored) {}
                            return true;
                        }
                        y += SETTING_ROW;
                    }
                }
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    private void openInput(Module mod, Field field, Number value, Setting setting) {
        inputModule = mod;
        inputField = field;
        inputTitle = setting.name().isEmpty() ? field.getName() : setting.name();
        inputText = formatNumber(value, setting);
    }

    private void cancelInput() {
        inputModule = null;
        inputField = null;
        inputText = "";
        inputTitle = "";
    }

    private void applyInput() {
        if (inputModule == null || inputField == null) {
            cancelInput();
            return;
        }
        try {
            inputField.setAccessible(true);
            Object value = inputField.get(inputModule);
            Setting s = inputField.getAnnotation(Setting.class);

            double parsed;
            try {
                parsed = Double.parseDouble(inputText);
            } catch (NumberFormatException e) {
                cancelInput();
                return;
            }
            if (s.min() != Double.MIN_VALUE) parsed = Math.max(s.min(), parsed);
            if (s.max() != Double.MAX_VALUE) parsed = Math.min(s.max(), parsed);

            if (value instanceof Integer) inputField.setInt(inputModule, (int) parsed);
            else if (value instanceof Double) inputField.setDouble(inputModule, parsed);
            else if (value instanceof Float) inputField.setFloat(inputModule, (float) parsed);
            else if (value instanceof Long) inputField.setLong(inputModule, (long) parsed);
            inputModule.saveConfig();
        } catch (Exception ignored) {}
        cancelInput();
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingPanel != null) {
            draggingPanel = null;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingPanel != null) {
            float scale = getActiveScale();
            draggingPanel.x = (int) (mx / scale) - dragDX;
            draggingPanel.y = (int) (my / scale) - dragDY;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        if (isCtrlPressed()) {
            ClickGUIModule cfg = getConfig();
            if (cfg != null) {
                cfg.autoScale = false;
                float newScale = (float) cfg.guiScale + (float) verticalAmount * SCALE_STEP;
                newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
                cfg.guiScale = newScale;
                cfg.saveConfig();
            }
            return true;
        }
        return super.mouseScrolled(mx, my, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputModule != null && inputField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelInput();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                applyInput();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!inputText.isEmpty()) {
                    inputText = inputText.substring(0, inputText.length() - 1);
                }
                return true;
            }
            return false;
        }

        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKey(-1);
            } else {
                bindingModule.setKey(keyCode);
            }
            bindingModule = null;
            return true;
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                searchQuery = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inputModule != null && inputField != null) {
            if ((chr >= '0' && chr <= '9') || chr == '.' || chr == '-') {
                inputText += chr;
            }
            return true;
        }

        if (searchFocused && chr >= 32 && chr != 127) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}