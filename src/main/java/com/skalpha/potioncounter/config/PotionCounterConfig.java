package com.skalpha.potioncounter.config;

import lombok.*;
import com.skalpha.potioncounter.PotionCounter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PotionCounterConfig implements Serializable {
    // === POTION DISPLAY CONFIG ===
    private boolean displayEnabled = true;
    private int x = 447;
    private int y = 443;
    private boolean useDefaultPotion = false;
    private boolean displayColors = true;
    private boolean coloredXpBar = false;
    private boolean alwaysShowBar = false;
    private boolean showPotCounter = false;

    // === POTS COUNTER CONFIG ===
    private boolean counterEnabled = true;
    private boolean separator = true;
    private boolean counterColors = true;
    private boolean showInTab = false;

    public static PotionCounterConfig get() {
        return PotionCounter.getManager().getConfig();
    }
}
