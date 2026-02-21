package com.skalpha.potioncounter.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import com.skalpha.potioncounter.PotionCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class MixinPlayer {
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    public Component appendCounterLunar(Component original) {
        Player self = (Player) (Object) this;
        return PotionCounter.showPotsInText(self, original);
    }
}
