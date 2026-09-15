package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.setting.Setting;

public class NoSlow extends Module {
    @Setting(name="Предметы") public boolean items = true;
    @Setting(name="Паутина") public boolean web = true;
    @Setting(name="Слизь") public boolean slimeBlock = true;
    @Setting(name="Мёд") public boolean honeyBlock = true;
    @Setting(name="Песок душ") public boolean soulSand = true;
    @Setting(name="Ягоды") public boolean berryBush = true;
    @Setting(name="Жидкости") public boolean fluidDrag = true;
    @Setting(name="Присед") public boolean sneaking = false;
    @Setting(name="Голод") public boolean hunger = false;
    @Setting(name="Замедление") public boolean slowness = false;

    public NoSlow() {
        super("NoSlow", "Убирает замедление", Category.MOVEMENT);
    }

    public boolean items() { return isEnabled() && items; }
    public boolean web() { return isEnabled() && web; }
    public boolean slimeBlock() { return isEnabled() && slimeBlock; }
    public boolean honeyBlock() { return isEnabled() && honeyBlock; }
    public boolean soulSand() { return isEnabled() && soulSand; }
    public boolean berryBush() { return isEnabled() && berryBush; }
    public boolean fluidDrag() { return isEnabled() && fluidDrag; }
    public boolean sneaking() { return isEnabled() && sneaking; }
    public boolean hunger() { return isEnabled() && hunger; }
    public boolean slowness() { return isEnabled() && slowness; }
}