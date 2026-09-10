package com.gothbreach.client.ui;

public class Theme {
    public final String name;
    public final int background, header, panel, button, buttonHover, text, accent, settingsBg, searchBg;

    public Theme(String name, int background, int header, int panel, int button,
                 int buttonHover, int text, int accent, int settingsBg, int searchBg) {
        this.name = name;
        this.background = background;
        this.header = header;
        this.panel = panel;
        this.button = button;
        this.buttonHover = buttonHover;
        this.text = text;
        this.accent = accent;
        this.settingsBg = settingsBg;
        this.searchBg = searchBg;
    }

    public static final Theme DEFAULT = new Theme(
        "Default",
        0xCC101010, 0xFF303030, 0xFF202020, 0xFF404040, 0xFF505050,
        0xFFFFFFFF, 0xFF00FF00, 0xDD303030, 0xFF181818
    );

    public static final Theme EVERFOREST = new Theme(
        "Everforest",
        0xCC232A2E, 0xFF2D353B, 0xFF272E33, 0xFF3A4248, 0xFF475258,
        0xFFD3C6AA, 0xFFA7C080, 0xDD2D353B, 0xFF1F2428
    );

    public static final Theme GRUVBOX = new Theme(
        "Gruvbox",
        0xCC282828, 0xFF3C3836, 0xFF32302F, 0xFF504945, 0xFF665C54,
        0xFFEBDBB2, 0xFFB8BB26, 0xDD3C3836, 0xFF1D2021
    );

    public static final Theme CATPPUCCIN = new Theme(
        "Catppuccin",
        0xCC1E1E2E, 0xFF313244, 0xFF282A36, 0xFF45475A, 0xFF585B70,
        0xFFCDD6F4, 0xFFA6E3A1, 0xDD313244, 0xFF181825
    );
}
