package com.gothbreach.client.ui;

import com.gothbreach.client.CheatClient;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    private Category selectedCategory = Category.COMBAT;
    private int scrollOffset = 0;
    private final int rowHeight = 20;
    private final int headerHeight = 24;

    public ClickGUI() {
        super(Text.literal("Gothbreach Client"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемнение фона
        context.fill(0, 0, width, height, 0x80000000);

        // Заголовок
        context.fill(10, 10, 400, 34, 0xFF303030);
        context.drawTextWithShadow(textRenderer, "Gothbreach Client", 16, 16, 0xFFFFFF);

        // Категории
        int catX = 10;
        int catY = 40;
        for (Category cat : Category.values()) {
            boolean selected = cat == selectedCategory;
            context.fill(catX, catY, catX + 80, catY + rowHeight, selected ? 0xFF00AA00 : 0xFF404040);
            context.drawTextWithShadow(textRenderer, cat.getName(), catX + 5, catY + 6, 0xFFFFFF);
            catY += rowHeight;
        }

        // Модули выбранной категории
        List<Module> modules = new ArrayList<>(CheatClient.moduleManager.getModulesInCategory(selectedCategory));
        int moduleX = 100;
        int moduleY = 40;
        int maxVisible = (height - 50) / rowHeight;
        int start = Math.max(0, scrollOffset);
        int end = Math.min(start + maxVisible, modules.size());
        for (int i = start; i < end; i++) {
            Module mod = modules.get(i);
            boolean enabled = mod.isEnabled();
            context.fill(moduleX, moduleY, moduleX + 150, moduleY + rowHeight, enabled ? 0xFF005500 : 0xFF404040);
            context.drawTextWithShadow(textRenderer, mod.getName(), moduleX + 5, moduleY + 6, enabled ? 0x00FF00 : 0xFFFFFF);
            moduleY += rowHeight;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Клики по категориям
        int catY = 40;
        for (Category cat : Category.values()) {
            if (mouseX >= 10 && mouseX <= 90 && mouseY >= catY && mouseY <= catY + rowHeight) {
                selectedCategory = cat;
                scrollOffset = 0;
                return true;
            }
            catY += rowHeight;
        }

        // Клики по модулям
        List<Module> modules = new ArrayList<>(CheatClient.moduleManager.getModulesInCategory(selectedCategory));
        int moduleY = 40;
        int maxVisible = (height - 50) / rowHeight;
        int start = Math.max(0, scrollOffset);
        int end = Math.min(start + maxVisible, modules.size());
        for (int i = start; i < end; i++) {
            if (mouseX >= 100 && mouseX <= 250 && mouseY >= moduleY && mouseY <= moduleY + rowHeight) {
                modules.get(i).toggle();
                return true;
            }
            moduleY += rowHeight;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Прокрутка списка модулей
        List<Module> modules = new ArrayList<>(CheatClient.moduleManager.getModulesInCategory(selectedCategory));
        int maxVisible = (height - 50) / rowHeight;
        if (modules.size() > maxVisible) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)verticalAmount, modules.size() - maxVisible));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() { return false; }
}