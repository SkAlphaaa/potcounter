package com.skalpha.potioncounter;

import net.minecraft.client.gui.screens.Screen;
import com.skalpha.potioncounter.config.PotionConfigScreen;
import net.uku3lig.ukulib.api.UkulibAPI;

import java.util.function.UnaryOperator;

public class UkulibHook implements UkulibAPI {
    @Override
    public UnaryOperator<Screen> supplyConfigScreen() {
        return PotionConfigScreen::new;
    }
}
