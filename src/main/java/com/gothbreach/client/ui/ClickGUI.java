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
import java.util.*;

public class ClickGUI extends Screen {
    // === Размеры (компактные, Meteor-style) ===
    private static final int PANEL_WIDTH = 120;
    private static final int ROW_HEIGHT = 13;
    private static final int HEADER_HEIGHT = 14;
    private static final int SETTING_ROW = 12;

    // === Цвета (тёмная тема) ===
    private static final int BG_DIM = 0x80000000;
    private static final int PANEL_BG = 0xF01A1A1A;
    private static final int HEADER_BG = 0xF02A2A2A;
    private static final int ROW_BG = 0xFF1E1E1E;
    private static final int ROW_HOVER = 0xFF252525;
    private static final int ROW_ENABLED = 0xFF2A2A2A;
    private static final int ROW_SELECTED = 0xFF3A3A3A;
    private static final int TEXT = 0xFFD0D0D0;
    private static final int TEXT_DIM = 0xFF707070;
    private static final int ACCENT = 0xFF3B82F6;
    private static final int SLIDER_BG = 0xFF151515;
    private static final int GREEN = 0xFF10B981;
    private static final int RED = 0xFFEF4444;
    private static final int SEARCH_BG = 0xFF1E1E1E;
    private static final int SEARCH_BORDER = 0xFF3B82F6;
    private static final int INPUT_BG = 0xFF0D0D0D;
    private static final int INPUT_BORDER = 0xFF3B82F6;

    // === Состояние ===
    private final Map<Category, Panel> panels = new LinkedHashMap<>();
    private Panel draggingPanel = null;
    private int dragDX, dragDY;

    private Module selectedModule = null;
    private Module bindingModule = null;

    private String searchQuery = "";
    private boolean searchFocused = false;

    // === Текстовый ввод ===
    private Module inputModule = null;
    private Field inputField = null;
    private String inputText = "";

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
        int x = 15;
        for (Category cat : Category.values()) {
            panels.put(cat, new Panel(cat, x, 20));
            x += PANEL_WIDTH + 4;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BG_DIM);

        renderSearch(ctx, mouseX, mouseY);

        for (Panel panel : panels.values()) {
            renderPanel(ctx, panel, mouseX, mouseY);
        }

        // Отрисовка активного текстового поля поверх всего
        if (inputModule != null && inputField != null) {
            renderInputField(ctx, mouseX, mouseY);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderSearch(DrawContext ctx, int mouseX, int mouseY) {
        int searchW = 160;
        int searchH = 14;
        int searchX = (width - searchW) / 2;
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
        ctx.drawTextWithShadow(textRenderer, panel.category.getName(), panel.x + 4, panel.y + 3, 0xFFFFFFFF);

        String indicator = panel.collapsed ? "+" : "-";
        int indW = textRenderer.getWidth(indicator);
        ctx.drawTextWithShadow(textRenderer, indicator, panel.x + PANEL_WIDTH - indW - 3, panel.y + 3, TEXT_DIM);

        if (panel.collapsed) return;

        List<Module> modules = getFilteredModules(panel.category);
        int y = panel.y + HEADER_HEIGHT;

        for (Module mod : modules) {
            boolean enabled = mod.isEnabled();
            boolean selected = mod == selectedModule;
            boolean hovered = mouseX >= panel.x && mouseX <= panel.x + PANEL_WIDTH
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

            int bg;
            if (selected) bg = ROW_SELECTED;
            else if (enabled) bg = ROW_ENABLED;
            else if (hovered) bg = ROW_HOVER;
            else bg = ROW_BG;

            ctx.fill(panel.x, y, panel.x + PANEL_WIDTH, y + ROW_HEIGHT, bg);

            if (enabled) {
                ctx.fill(panel.x, y, panel.x + 2, y + ROW_HEIGHT, ACCENT);
            }

            int textColor = enabled ? 0xFFFFFFFF : TEXT;
            ctx.drawTextWithShadow(textRenderer, mod.getName(), panel.x + 5, y + 2, textColor);

            if (mod.getKey() != -1) {
                String keyName = getKeyName(mod.getKey());
                if (keyName != null) {
                    int kw = textRenderer.getWidth(keyName);
                    ctx.drawTextWithShadow(textRenderer, keyName, panel.x + PANEL_WIDTH - kw - 3, y + 2, TEXT_DIM);
                }
            }

            y += ROW_HEIGHT;

            if (selected) {
                y = renderSettings(ctx, mod, panel.x, y, mouseX, mouseY);
            }
        }
    }

    private int renderSettings(DrawContext ctx, Module mod, int panelX, int y, int mouseX, int mouseY) {
        y = renderBindRow(ctx, mod, panelX, y, mouseX, mouseY);

        List<Field> fields = getSettingFields(mod);
        for (Field field : fields) {
            Setting s = field.getAnnotation(Setting.class);
            try {
                field.setAccessible(true);
                Object value = field.get(mod);
                String name = s.name().isEmpty() ? field.getName() : s.name();

                boolean hovered = mouseX >= panelX + 2 && mouseX <= panelX + PANEL_WIDTH - 2
                        && mouseY >= y && mouseY < y + SETTING_ROW;

                int bg = hovered ? 0xFF202020 : 0xFF161616;
                ctx.fill(panelX + 2, y, panelX + PANEL_WIDTH - 2, y + SETTING_ROW, bg);

                ctx.drawTextWithShadow(textRenderer, name, panelX + 5, y + 2, TEXT);

                if (value instanceof Boolean b) {
                    String txt = b ? "ON" : "OFF";
                    int color = b ? GREEN : RED;
                    int w = textRenderer.getWidth(txt);
                    ctx.drawTextWithShadow(textRenderer, txt, panelX + PANEL_WIDTH - w - 5, y + 2, color);
                } else if (value instanceof Number n) {
                    String txt = formatNumber(n, s);
                    int w = textRenderer.getWidth(txt);
                    // Если это активное поле ввода — рисуем по-другому
                    boolean isInput = (mod == inputModule && field == inputField);
                    int txtColor = isInput ? 0xFFFFAA00 : ACCENT;
                    ctx.drawTextWithShadow(textRenderer, txt, panelX + PANEL_WIDTH - w - 5, y + 2, txtColor);

                    // Слайдер
                    double range = s.max() - s.min();
                    if (range > 0) {
                        double num = n.doubleValue();
                        double norm = Math.max(0, Math.min(1, (num - s.min()) / range));
                        int sliderY = y + SETTING_ROW - 2;
                        int sliderX = panelX + 3;
                        int sliderW = PANEL_WIDTH - 6;
                        ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 1, SLIDER_BG);
                        ctx.fill(sliderX, sliderY, sliderX + (int)(norm * sliderW), sliderY + 1, ACCENT);
                    }
                } else if (value instanceof String str) {
                    int w = textRenderer.getWidth(str);
                    ctx.drawTextWithShadow(textRenderer, str, panelX + PANEL_WIDTH - w - 5, y + 2, ACCENT);
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
        else if (hovered) bg = 0xFF202020;
        else bg = 0xFF161616;

        ctx.fill(panelX + 2, y, panelX + PANEL_WIDTH - 2, y + SETTING_ROW, bg);
        ctx.drawTextWithShadow(textRenderer, "Bind", panelX + 5, y + 2, TEXT);

        String keyName = isBinding ? "..." :
                (mod.getKey() == -1 ? "None" : getKeyName(mod.getKey()));
        if (keyName == null) keyName = "None";
        int color = isBinding ? 0xFFFFAA00 : (mod.getKey() == -1 ? TEXT_DIM : ACCENT);
        int w = textRenderer.getWidth(keyName);
        ctx.drawTextWithShadow(textRenderer, keyName, panelX + PANEL_WIDTH - w - 5, y + 2, color);

        return y + SETTING_ROW;
    }

    private void renderInputField(DrawContext ctx, int mouseX, int mouseY) {
        // Определяем позицию поля на основе настроек модуля
        Panel panel = null;
        for (Panel p : panels.values()) {
            if (p.category == inputModule.getCategory()) {
                panel = p;
                break;
            }
        }
        if (panel == null || panel.collapsed) return;

        List<Module> modules = getFilteredModules(panel.category);
        int y = panel.y + HEADER_HEIGHT;
        for (Module mod : modules) {
            if (mod == inputModule) {
                y += ROW_HEIGHT; // после модуля
                y = renderBindRowHeight(y); // Bind
                for (Field field : getSettingFields(mod)) {
                    if (field == inputField) {
                        int x = panel.x + PANEL_WIDTH - 60;
                        int w = 55;
                        int h = SETTING_ROW - 2;
                        ctx.fill(x, y - 1, x + w, y + h, INPUT_BG);
                        ctx.fill(x, y - 1, x + w, y, INPUT_BORDER);
                        ctx.fill(x, y + h - 1, x + w, y + h, INPUT_BORDER);
                        ctx.fill(x, y - 1, x + 1, y + h, INPUT_BORDER);
                        ctx.fill(x + w - 1, y - 1, x + w, y + h, INPUT_BORDER);
                        ctx.drawTextWithShadow(textRenderer, inputText + "_", x + 3, y + 1, 0xFFFFFFFF);
                        return;
                    }
                    y += SETTING_ROW;
                }
            } else {
                y += ROW_HEIGHT;
                if (mod == selectedModule) {
                    y = renderBindRowHeight(y);
                    y += getSettingFields(mod).size() * SETTING_ROW;
                }
            }
        }
    }

    private int renderBindRowHeight(int y) {
        return y + SETTING_ROW;
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
        int mouseX = (int) mx;
        int mouseY = (int) my;

        // Если открыто поле ввода — клик вне него закрывает его
        if (inputModule != null) {
            // Проверяем, попал ли клик в поле
            if (isClickInInputField(mouseX, mouseY)) {
                return true;
            } else {
                applyInput();
                return true;
            }
        }

        // Поиск
        int searchW = 160;
        int searchX = (width - searchW) / 2;
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
                        && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                    if (button == 0) mod.toggle();
                    else if (button == 1) {
                        selectedModule = (selectedModule == mod) ? null : mod;
                        bindingModule = null;
                        if (inputModule != null) applyInput();
                    }
                    return true;
                }
                y += ROW_HEIGHT;

                if (mod == selectedModule) {
                    // Bind
                    if (mouseX >= panel.x + 2 && mouseX <= panel.x + PANEL_WIDTH - 2
                            && mouseY >= y && mouseY < y + SETTING_ROW) {
                        bindingModule = (bindingModule == mod) ? null : mod;
                        if (inputModule != null) applyInput();
                        return true;
                    }
                    y += SETTING_ROW;

                    // Настройки
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
                                    // Определяем, куда попал клик: на слайдер или на текст
                                    int valueX = panel.x + PANEL_WIDTH - 60;
                                    if (mouseX >= valueX) {
                                        // Клик по значению — открываем ввод
                                        if (inputModule != null) applyInput();
                                        inputModule = mod;
                                        inputField = field;
                                        inputText = formatNumber((Number) value, s);
                                    } else {
                                        // Клик по слайдеру — меняем значение
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

    private boolean isClickInInputField(int mouseX, int mouseY) {
        if (inputModule == null || inputField == null) return false;
        Panel panel = panels.get(inputModule.getCategory());
        if (panel == null || panel.collapsed) return false;
        List<Module> modules = getFilteredModules(panel.category);
        int y = panel.y + HEADER_HEIGHT;
        for (Module mod : modules) {
            if (mod == inputModule) {
                y += ROW_HEIGHT;
                y += SETTING_ROW; // Bind
                for (Field field : getSettingFields(mod)) {
                    if (field == inputField) {
                        int x = panel.x + PANEL_WIDTH - 60;
                        int w = 55;
                        int h = SETTING_ROW - 2;
                        return mouseX >= x && mouseX <= x + w && mouseY >= y - 1 && mouseY <= y + h;
                    }
                    y += SETTING_ROW;
                }
            } else {
                y += ROW_HEIGHT;
                if (mod == selectedModule) {
                    y += SETTING_ROW;
                    y += getSettingFields(mod).size() * SETTING_ROW;
                }
            }
        }
        return false;
    }

    private void applyInput() {
        if (inputModule == null || inputField == null) {
            inputModule = null;
            inputField = null;
            inputText = "";
            return;
        }
        try {
            inputField.setAccessible(true);
            Object value = inputField.get(inputModule);
            Setting s = inputField.getAnnotation(Setting.class);
            double parsed = Double.parseDouble(inputText);
            parsed = Math.max(s.min(), Math.min(s.max(), parsed));

            if (value instanceof Integer) inputField.setInt(inputModule, (int) parsed);
            else if (value instanceof Double) inputField.setDouble(inputModule, parsed);
            else if (value instanceof Float) inputField.setFloat(inputModule, (float) parsed);
            else if (value instanceof Long) inputField.setLong(inputModule, (long) parsed);
            inputModule.saveConfig();
        } catch (Exception ignored) {}
        inputModule = null;
        inputField = null;
        inputText = "";
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
            draggingPanel.x = (int) mx - dragDX;
            draggingPanel.y = (int) my - dragDY;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Режим ввода числа
        if (inputModule != null && inputField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                inputModule = null;
                inputField = null;
                inputText = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                applyInput();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!inputText.isEmpty()) {
                    inputText = inputText.substring(0, inputText.length() - 1);
                }
                return true;
            }
            return true;
        }

        // Режим бинда
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKey(-1);
            } else {
                bindingModule.setKey(keyCode);
            }
            bindingModule = null;
            return true;
        }

        // Поиск
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
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Ввод числа
        if (inputModule != null && inputField != null) {
            if (chr >= '0' && chr <= '9' || chr == '.' || chr == '-') {
                inputText += chr;
            }
            return true;
        }

        // Поиск
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