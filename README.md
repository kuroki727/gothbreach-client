<div align="center">

# 🎮 Gothbreach Client

**A powerful Fabric-based Minecraft utility client for 1.20.4**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-Loader-DBB69B?style=for-the-badge)](https://fabricmc.net)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

[English](#-english) • [Русский](#-русский)

</div>

---

## 🇬🇧 English

### 📖 Table of Contents

- [About](#about)
- [Features](#features)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Usage](#usage)
- [Configuration](#configuration)
- [Module List](#module-list)
- [Contributing](#contributing)
- [License](#license)

---

### 🎯 About

**Gothbreach Client** is a modern Minecraft utility client built on the Fabric mod loader. It provides a wide range of modules for PvP, movement, rendering, and world interaction, with a focus on flexibility, stability, and clean design.

The client is designed with a RusherHack-inspired interface: dark theme, customizable colors, tabbed categories, and quick configuration via an in-game GUI.

---

### ✨ Features

- 🎨 **Modern ClickGUI** — dark theme, rounded corners, custom accent color, blur support
- ⚙️ **Fully customizable modules** — dozens of settings per module
- 🎹 **Keybinds** — bind any module to any key
- 💾 **Auto-saving config** — all settings persist between sessions
- 🌍 **Multi-language support** — English and Russian built-in, custom languages via JSON
- 🚀 **Blazing fast** — event-based architecture with minimal overhead
- 🔧 **Modular design** — easy to extend with new modules

---

### 🛠 Installation

1. Install **Minecraft 1.20.4**
2. Install **Fabric Loader** from [fabricmc.net](https://fabricmc.net/use/)
3. Download **Fabric API** from [Modrinth](https://modrinth.com/mod/fabric-api)
4. Drop `gothbreach-client-x.x.x.jar` and `fabric-api-x.x.x.jar` into `.minecraft/mods/`
5. Launch Minecraft with the Fabric profile

---

### 🏗 Building from Source

**Requirements:**
- JDK 17 or higher
- Gradle 8.6+
- Git

**Build steps:**

```bash
git clone https://github.com/kuroki727/gothbreach-client.git
cd gothbreach-client
./gradlew remapJar
```

The compiled `.jar` will appear in `build/libs/`.

---

### 🎮 Usage

| Action | Default Key |
|---|---|
| Open GUI | `Right Shift` |
| Toggle module | `Left Click` on module |
| Open settings | `Right Click` on module |
| Bind key | Click "Bind" in settings |
| Change slider | Drag with mouse |
| Change option | Click the value |

---

### ⚙ Configuration

All settings, keybinds, and module states are automatically saved to:

```
.minecraft/gothbreach_config/
├── modules/
│   ├── KillAura.json
│   ├── CrystalAura.json
│   └── ...
└── lang/
    └── custom_lang.json
```

Custom language files can be added as JSON:

```json
{
  "category.combat": "Combat",
  "module.killaura": "KillAura"
}
```

---

### 📦 Module List

#### ⚔ Combat
- **KillAura** — automatic attack with CPS, priority, silent rotation, multi-target, weapon auto-switch
- **CrystalAura** — auto crystal placement and detonation with damage prediction
- **AutoTotem** — automatic totem swapping
- **AutoArmor** — equips best armor automatically
- **Criticals** — guaranteed critical hits
- **Reach** — extended reach distance
- **Surround** — places blocks around you
- **AutoTrap** — traps enemies with obsidian
- **HoleFill** — fills holes around enemies
- **Burrow** — places a block inside yourself
- **AutoLog** — disconnects on low health
- **AutoCity** — breaks obsidian around enemies
- **AutoPearl** — throws ender pearl when falling

#### 🏃 Movement
- **Speed** — multiple speed modes
- **Fly** — creative-style flight
- **PacketFly** — packet-based flight
- **NoSlow** — removes slowdown from items, blocks, liquids
- **Step** — auto step-up blocks
- **Jesus** — walk on water
- **Sprint** — auto sprint
- **Strafe** — improved strafing
- **ElytraFly** — advanced elytra control with hover, boost, and modes
- **Scaffold** — automatic block placement with tower modes

#### 👤 Player
- Coming soon: AutoEat, AutoRespawn, AntiVoid, Blink, Freecam, FastPlace

#### 🎨 Render
- **ClickGUI** — settings for the interface itself (theme, colors, scale)

#### 🌍 World
- Coming soon: Timer, FastBreak, Nuker, AutoTool

#### 🔧 Misc
- Coming soon: Baritone Integration, AntiAim, FakeLag, and more

---

### 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

### 📜 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**[⬆ Back to top](#-gothbreach-client)**

</div>

---

## 🇷🇺 Русский

### 📖 Содержание

- [О проекте](#о-проекте)
- [Возможности](#возможности)
- [Установка](#установка)
- [Сборка из исходников](#сборка-из-исходников)
- [Использование](#использование)
- [Конфигурация](#конфигурация)
- [Список модулей](#список-модулей)
- [Участие в разработке](#участие-в-разработке)
- [Лицензия](#лицензия)

---

### 🎯 О проекте

**Gothbreach Client** — современный чит-клиент для Minecraft на базе Fabric. Предоставляет широкий набор модулей для PvP, передвижения, рендеринга и взаимодействия с миром, с упором на гибкость, стабильность и чистый дизайн.

Интерфейс выполнен в стиле RusherHack: тёмная тема, настраиваемые цвета, вкладки категорий и быстрое управление прямо из игры.

---

### ✨ Возможности

- 🎨 **Современный ClickGUI** — тёмная тема, закруглённые углы, свой акцентный цвет, размытие фона
- ⚙️ **Полная настройка модулей** — десятки параметров для каждого
- 🎹 **Бинды клавиш** — привязка любого модуля к любой клавише
- 💾 **Автосохранение конфига** — все настройки сохраняются между сессиями
- 🌍 **Мультиязычность** — английский и русский встроены, можно добавлять свои языки через JSON
- 🚀 **Быстрый** — событийная архитектура с минимальными накладными расходами
- 🔧 **Модульная структура** — легко добавлять новые модули

---

### 🛠 Установка

1. Установи **Minecraft 1.20.4**
2. Установи **Fabric Loader** с [fabricmc.net](https://fabricmc.net/use/)
3. Скачай **Fabric API** с [Modrinth](https://modrinth.com/mod/fabric-api)
4. Положи `gothbreach-client-x.x.x.jar` и `fabric-api-x.x.x.jar` в `.minecraft/mods/`
5. Запусти Minecraft через профиль Fabric

---

### 🏗 Сборка из исходников

**Требования:**
- JDK 17 или выше
- Gradle 8.6+
- Git

**Шаги сборки:**

```bash
git clone https://github.com/kuroki727/gothbreach-client.git
cd gothbreach-client
./gradlew remapJar
```

Готовый `.jar` появится в `build/libs/`.

---

### 🎮 Использование

| Действие | Клавиша по умолчанию |
|---|---|
| Открыть GUI | `Right Shift` |
| Включить/выключить модуль | `ЛКМ` по модулю |
| Открыть настройки | `ПКМ` по модулю |
| Привязать клавишу | Нажать «Бинд» в настройках |
| Изменить слайдер | Перетащить мышью |
| Изменить опцию | Клик по значению |

---

### ⚙ Конфигурация

Все настройки, бинды и состояния модулей автоматически сохраняются в:

```
.minecraft/gothbreach_config/
├── modules/
│   ├── KillAura.json
│   ├── CrystalAura.json
│   └── ...
└── lang/
    └── custom_lang.json
```

Свои языковые файлы можно добавлять в формате JSON:

```json
{
  "category.combat": "Бой",
  "module.killaura": "Аура"
}
```

---

### 📦 Список модулей

#### ⚔ Бой
- **KillAura** — авто-атака с CPS, приоритетом, тихой ротацией, мульти-целями, авто-выбором оружия
- **CrystalAura** — авто-установка и подрыв кристаллов с предсказанием урона
- **AutoTotem** — авто-замена тотема
- **AutoArmor** — авто-надевание лучшей брони
- **Criticals** — гарантированные криты
- **Reach** — увеличенная дальность
- **Surround** — обкладывает вас блоками
- **AutoTrap** — ловушка для врага из обсидиана
- **HoleFill** — заполняет дыры вокруг врагов
- **Burrow** — ставит блок в себя
- **AutoLog** — выходит при низком HP
- **AutoCity** — ломает обсидиан вокруг врага
- **AutoPearl** — кидает жемчуг при падении

#### 🏃 Движение
- **Speed** — несколько режимов ускорения
- **Fly** — креативный полёт
- **PacketFly** — полёт через пакеты
- **NoSlow** — убирает замедление от предметов, блоков, жидкостей
- **Step** — авто-забирание на блоки
- **Jesus** — хождение по воде
- **Sprint** — авто-спринт
- **Strafe** — улучшенный стрейф
- **ElytraFly** — продвинутое управление элитрами с зависанием, бустом и режимами
- **Scaffold** — авто-установка блоков с режимами башни

#### 👤 Игрок
- Скоро: AutoEat, AutoRespawn, AntiVoid, Blink, Freecam, FastPlace

#### 🎨 Рендер
- **ClickGUI** — настройки самого интерфейса (тема, цвета, масштаб)

#### 🌍 Мир
- Скоро: Timer, FastBreak, Nuker, AutoTool

#### 🔧 Разное
- Скоро: Baritone Integration, AntiAim, FakeLag и другие

---

### 🤝 Участие в разработке

Pull request'ы приветствуются. Для крупных изменений сначала открой issue и обсуди, что хочешь изменить.

---

### 📜 Лицензия

Проект распространяется под лицензией MIT — см. файл [LICENSE](LICENSE).

---

<div align="center">

**[⬆ Наверх](#-gothbreach-client)**

</div>
