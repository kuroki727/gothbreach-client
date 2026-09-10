package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.setting.Setting;

public class NoSlow extends Module {
    @Setting(name="Items") public boolean items = true;
    @Setting(name="Web") public boolean web = true;
    @Setting(name="Honey Block") public boolean honeyBlock = true;
    @Setting(name="Soul Sand") public boolean soulSand = true;
    @Setting(name="Slime Block") public boolean slimeBlock = true;
    @Setting(name="Berry Bush") public boolean berryBush = true;
    @Setting(name="Fluid Drag") public boolean fluidDrag = true;
    @Setting(name="Sneaking") public boolean sneaking = false;
    @Setting(name="Hunger") public boolean hunger = false;
    @Setting(name="Slowness") public boolean slowness = false;

    public NoSlow() {
        super("NoSlow", "Убирает замедление", Category.MOVEMENT);
    }

    // Геттеры для миксинов
    public boolean items() { return isEnabled() && items; }
    public boolean web() { return isEnabled() && web; }
    public boolean honeyBlock() { return isEnabled() && honeyBlock; }
    public boolean soulSand() { return isEnabled() && soulSand; }
    public boolean slimeBlock() { return isEnabled() && slimeBlock; }
    public boolean berryBush() { return isEnabled() && berryBush; }
    public boolean fluidDrag() { return isEnabled() && fluidDrag; }
    public boolean sneaking() { return isEnabled() && sneaking; }
    public boolean hunger() { return isEnabled() && hunger; }
    public boolean slowness() { return isEnabled() && slowness; }
}