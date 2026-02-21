package com.skalpha.potioncounter.config;

import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.Screen;
import com.skalpha.potioncounter.PotionCounter;
import net.uku3lig.ukulib.config.option.CyclingOption;
import net.uku3lig.ukulib.config.option.ScreenOpenButton;
import net.uku3lig.ukulib.config.option.SimpleButton;
import net.uku3lig.ukulib.config.option.WidgetCreator;
import net.uku3lig.ukulib.config.option.widget.ButtonTab;
import net.uku3lig.ukulib.config.screen.TabbedConfigScreen;

public class PotionConfigScreen extends TabbedConfigScreen<PotionCounterConfig> {
    public PotionConfigScreen(Screen parent) {
        super("potioncounter.config", parent, PotionCounter.getManager());
    }

    @Override
    protected Tab[] getTabs(PotionCounterConfig config) {
        return new Tab[]{
                new PotCounterTab(), new PotionDisplayTab(),
        };
    }

    public class PotCounterTab extends ButtonTab<PotionCounterConfig> {
        public PotCounterTab() {
            super("potioncounter.config.pot", PotionConfigScreen.this.manager);
        }

        @Override
        public WidgetCreator[] getWidgets(PotionCounterConfig config) {
            return new WidgetCreator[]{
                    CyclingOption.ofBoolean("potioncounter.config.enabled", config.isCounterEnabled(), config::setCounterEnabled),
                    CyclingOption.ofBoolean("potioncounter.config.pot.separator", config.isSeparator(), config::setSeparator),
                    CyclingOption.ofBoolean("potioncounter.config.colors", config.isCounterColors(), config::setCounterColors),
                    CyclingOption.ofBoolean("potioncounter.config.tab", config.isShowInTab(), config::setShowInTab),
                    new SimpleButton("potioncounter.reset", b -> PotionCounter.resetPopCounter())
            };
        }
    }

    public class PotionDisplayTab extends ButtonTab<PotionCounterConfig> {
        public PotionDisplayTab() {
            super("potioncounter.config.display", PotionConfigScreen.this.manager);
        }

        @Override
        public WidgetCreator[] getWidgets(PotionCounterConfig config) {
            return new WidgetCreator[]{
                    CyclingOption.ofBoolean("potioncounter.config.enabled", config.isDisplayEnabled(), config::setDisplayEnabled),
                    new ScreenOpenButton("ukulib.position", parent -> new DisplayPositionSelectScreen(parent, config)),
                    CyclingOption.ofBoolean("potioncounter.config.display.defaultPotion", config.isUseDefaultPotion(), config::setUseDefaultPotion),
                    CyclingOption.ofBoolean("potioncounter.config.colors", config.isDisplayColors(), config::setDisplayColors),
                    CyclingOption.ofBoolean("potioncounter.config.display.coloredXpBar", config.isColoredXpBar(), config::setColoredXpBar),
                    CyclingOption.ofBoolean("potioncounter.config.display.alwaysShowBar", config.isAlwaysShowBar(), config::setAlwaysShowBar),
                    CyclingOption.ofBoolean("potioncounter.config.display.showPotsCounter", config.isShowPotCounter(), config::setShowPotCounter)
            };
        }
    }
}
