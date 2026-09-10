package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.setting.Setting;

public class Reach extends Module {
    @Setting(name = "Дальность", min = 3.0, max = 8.0, decimalPlaces = 1)
    public double reachDistance = 6.0;

    public Reach() {
        super("Reach", "Увеличенная дальность атаки и взаимодействия", Category.COMBAT);
    }
}