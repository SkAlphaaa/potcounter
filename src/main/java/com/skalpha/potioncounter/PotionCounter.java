package com.skalpha.potioncounter;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import com.skalpha.potioncounter.config.PotionCounterConfig;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.PlayerArgumentType;
import net.uku3lig.ukulib.utils.Ukutils;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static net.minecraft.ChatFormatting.*;

public class PotionCounter implements ClientModInitializer {
    private static final String MOD_ID = "potioncounter";

    @Getter
    private static final Map<UUID, Integer> pops = new HashMap<>();
    @Getter
    private static final ConfigManager<PotionCounterConfig> manager = ConfigManager.createDefault(PotionCounterConfig.class, MOD_ID);

    private static final KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("potioncounter", "key"));
    private static final KeyMapping resetCounter = new KeyMapping("potioncounter.reset", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, category);
    //no single fucking idea wt this custom thing does so i will leave it untill someone reports i hate fabric fuck me
    private static final List<Identifier> CUSTOM_POTIONS = List.of(Identifier.fromNamespaceAndPath("skypot", "totem_of_void_undying"));

    public static final ItemStack POTION = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.STRONG_HEALING);
    public static final Identifier DEFAULT_POTION = Identifier.fromNamespaceAndPath(MOD_ID, "gui/potion.png");
    public static final Identifier WHITE_BAR = Identifier.fromNamespaceAndPath(MOD_ID, "gui/bar.png");

    private static final Component PREFIX = Component.empty()
            .append(Component.literal("Potion").withStyle(RED, BOLD))
            .append(Component.literal("Counter").withStyle(DARK_RED, BOLD))
            .append(Component.literal(" » ").withStyle(GRAY, BOLD))
            .append(Component.empty().withStyle(RESET));
    private static final Component HEADER = Component.empty()
            .append(Component.literal(" ====== ").withStyle(GRAY))
            .append(Component.literal("Potion").withStyle(RED, BOLD))
            .append(Component.literal("Counter").withStyle(DARK_RED, BOLD))
            .append(Component.literal(" ====== ").withStyle(GRAY))
            .append(Component.empty().withStyle(RESET));
    private static final String PLAYER_ARG = "player";

    @Override
    public void onInitializeClient() {
        Ukutils.registerKeybinding(resetCounter, client -> resetPopCounter());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("resetcounter").executes(this::resetCounterCommand).then(
                    argument(PLAYER_ARG, PlayerArgumentType.player()).executes(this::resetPlayerCounterCommand)
            ));

            dispatcher.register(literal("showpots").executes(this::showPotsCommand).then(
                    argument(PLAYER_ARG, PlayerArgumentType.player()).executes(this::showPlayerPotsCommand)
            ));
        });
    }

    private int resetCounterCommand(CommandContext<FabricClientCommandSource> context) {
        PotionCounter.resetPopCounter();

        Component message = PREFIX.copy().append(Component.translatable("potioncounter.reset.success"));
        context.getSource().sendFeedback(message);
        return 0;
    }

    private int resetPlayerCounterCommand(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Player player = PlayerArgumentType.getPlayer(PLAYER_ARG, context);
        pops.remove(player.getUUID());

        Component message = PREFIX.copy().append(Component.translatable("potioncounter.reset.player", player.getScoreboardName()).withStyle(Style.EMPTY.withColor(GREEN)));
        context.getSource().sendFeedback(message);
        return 0;
    }

    private int showPotsCommand(CommandContext<FabricClientCommandSource> context) {
        if (pops.isEmpty()) {
            context.getSource().sendFeedback(PREFIX.copy().append(Component.translatable("potioncounter.show.noPops")));
        } else {
            context.getSource().sendFeedback(HEADER);
            pops.forEach((uuid, popCount) -> {
                Player player = context.getSource().getWorld().getPlayerByUUID(uuid);
                Component text = (player != null ? player.getDisplayName().copy() : Component.literal(uuid.toString())).withStyle(DARK_AQUA)
                        .append(Component.literal(": ").withStyle(GRAY))
                        .append(Component.literal("-" + popCount).withStyle(Style.EMPTY.withColor(PotionCounter.getPotsColor(popCount))));
                context.getSource().sendFeedback(text);
            });
        }

        return 0;
    }

    private int showPlayerPotsCommand(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Player player = PlayerArgumentType.getPlayer(PLAYER_ARG, context);
        Component playerName = player.getDisplayName().copy().withStyle(DARK_AQUA);
        int popCount = pops.getOrDefault(player.getUUID(), 0);
        MutableComponent text = PREFIX.copy();

        if (popCount == 0) {
            text.append(Component.translatable("potioncounter.show.player.noPots", playerName));
        } else {
            Component potText = Component.literal(String.valueOf(popCount)).withStyle(Style.EMPTY.withColor(PotionCounter.getPotsColor(popCount)));
            text.append(Component.translatable("potioncounter.show.player", playerName, potText));
        }

        context.getSource().sendFeedback(text);
        return 0;
    }

    public static int getCount(Player player) {
        if (player == null) return 0;
        if (PotionCounterConfig.get().isShowPotCounter())
            return PotionCounter.getPops().getOrDefault(player.getUUID(), 0);

        Inventory inv = player.getInventory();
        ItemStack offhand = inv.getItem(Inventory.SLOT_OFFHAND);

        return Stream.concat(inv.getNonEquipmentItems().stream(), Stream.of(offhand))
                .filter(PotionCounter::isPotion)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static boolean isPotion(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.SPLASH_POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null && contents.is(Potions.STRONG_HEALING)) {
                return true;
            }
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return CUSTOM_POTIONS.contains(id);
    }

    public static int getColor(int count) {
        if (!PotionCounterConfig.get().isDisplayColors()) return 0xFFFFFFFF;
        return PotionCounterConfig.get().isShowPotCounter() ? PotionCounter.getPotsColor(count) : PotionCounter.getPotionColor(count);
    }

    public static int getPotsColor(int pops) {
        return switch (pops) {
            case 1, 2 -> 0xFF55FF55; // light green
            case 3, 4 -> 0xFF00AA00; // dark green
            case 5, 6 -> 0xFFFFFF55; // yellow
            case 7, 8 -> 0xFFFFAA00; // gold
            default -> 0xFFFF5555; // red
        };
    }

    public static int getPotionColor(int amount) {
        return switch (amount) {
            case 1, 2 -> 0xFFFF5555; // red
            case 3, 4 -> 0xFFFFAA00; // gold
            case 5, 6 -> 0xFFFFFF55; // yellow
            case 7, 8 -> 0xFF00AA00; // dark green
            default -> 0xFF55FF55; // light green
        };
    }

    public static Component showPotsInText(Player entity, Component text) {
        PotionCounterConfig config = PotionCounter.getManager().getConfig();
        if (PotionCounter.getPops().containsKey(entity.getUUID()) && config.isCounterEnabled()) {
            int pops = PotionCounter.getPops().get(entity.getUUID());

            MutableComponent label = text.copy().append(" ");
            MutableComponent counter = Component.literal("-" + pops);
            if (config.isSeparator()) label.append(Component.literal("| ").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
            if (config.isCounterColors()) counter.setStyle(Style.EMPTY.withColor(PotionCounter.getPotsColor(pops)));
            label.append(counter);
            text = label;
        }

        return text;
    }

    public static void resetPopCounter() {
        pops.clear();
        Ukutils.sendToast(Component.nullToEmpty("Successfully reset pot counter"), Component.nullToEmpty("You can now start counting again!"));
    }
}
