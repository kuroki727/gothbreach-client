package com.gothbreach.client.module;

import com.gothbreach.client.util.LanguageManager;

public enum Category {
    COMBAT("category.combat"),
    MOVEMENT("category.movement"),
    PLAYER("category.player"),
    RENDER("category.render"),
    WORLD("category.world"),
    MISC("category.misc"),
    CLIENT("category.client");

    private final String translationKey;
    Category(String translationKey) { this.translationKey = translationKey; }
    public String getName() { return LanguageManager.get(translationKey); }
}