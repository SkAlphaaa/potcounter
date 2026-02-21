package com.skalpha.potioncounter.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.skalpha.potioncounter.PotionCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerTabOverlay {
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    public Component addPopCounter(Component original, @Local(argsOnly = true) PlayerInfo entry) {
        if (!PotionCounter.getManager().getConfig().isShowInTab()) return original;

        Level world = Minecraft.getInstance().level;
        if (world != null) {
            Player entity = world.getPlayerByUUID(entry.getProfile().id());
            if (entity != null) {
                if (!entity.isAlive()) PotionCounter.getPops().remove(entity.getUUID());
                return PotionCounter.showPotsInText(entity, original);
            }
        }

        return original;
    }
}
