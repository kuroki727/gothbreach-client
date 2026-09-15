package com.gothbreach.client.module;

import com.gothbreach.client.module.modules.combat.*;
import com.gothbreach.client.module.modules.movement.*;

import java.util.*;

public class ModuleManager {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public void init() {
        // Combat
        add(new KillAura());
        add(new CrystalAura());
        add(new AutoTotem());
        add(new AutoArmor());
        add(new Criticals());
        add(new Reach());
        add(new Surround());
        add(new AutoTrap());
        add(new HoleFill());
        add(new Burrow());
        add(new AutoLog());
        add(new AutoCity());
        add(new AutoPearl());

        // Movement
        add(new Speed());
        add(new Fly());
        add(new PacketFly());
        add(new NoSlow());
        add(new Step());
        add(new Jesus());
        add(new Sprint());
        add(new Strafe());
        add(new ElytraFly());
        add(new Scaffold());

        // Загружаем сохранённые настройки
        loadConfigs();
    }

    public void add(Module module) {
        modules.put(module.getName().toLowerCase(), module);
        com.gothbreach.client.event.EventManager.register(module);
    }

    public Module get(String name) {
        return modules.get(name.toLowerCase());
    }

    public Collection<Module> getAll() {
        return modules.values();
    }

    public List<Module> getModulesInCategory(Category cat) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules.values()) {
            if (m.getCategory() == cat) result.add(m);
        }
        return result;
    }

    public void saveConfigs() {
        for (Module m : modules.values()) m.saveConfig();
    }

    public void loadConfigs() {
        for (Module m : modules.values()) m.loadConfig();
    }
}