package com.skalpha.potioncounter.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import com.skalpha.potioncounter.PotionCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow
    private ClientLevel level;

    @Unique
    private final Set<Integer> potioncounter$processedPotions = new HashSet<>();

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void onEntityDataSync(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (this.level == null) return;

        Entity entity = this.level.getEntity(packet.id());

        if (!(entity instanceof AbstractThrownPotion potion)) return;
        if (potioncounter$processedPotions.contains(entity.getId())) return;
        ItemStack stack = potion.getItem();
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (!contents.hasEffects()) return;

        boolean isInstantHealth = false;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().value() == MobEffects.INSTANT_HEALTH.value()) {
                isInstantHealth = true;
                break;
            }
        }

        if (!isInstantHealth) return;
        potioncounter$processedPotions.add(entity.getId());
        Entity owner = potion.getOwner();

        if (owner instanceof Player player) {
            UUID uuid = player.getUUID();
            PotionCounter.getPops().compute(uuid, (u, i) -> i == null ? 1 : i + 1);
        } else {
            System.out.println("[Potion Counter] Unknown Owner Detected :<");
        }
    }
}