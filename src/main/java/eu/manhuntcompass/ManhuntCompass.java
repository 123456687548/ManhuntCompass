package eu.manhuntcompass;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;


import static net.minecraft.network.MessageType.CHAT;
import static net.minecraft.server.command.CommandManager.literal;

public enum ManhuntCompass {
    INSTANCE;

    private ServerPlayerEntity trackedPlayer;
    private BlockPos lastOverworldPos;
    private BlockPos lastNetherPos;
    private BlockPos lastEndPos;
    private MinecraftServer server;

    private final String OVERWORLD_STRING = "minecraft:overworld";
    private final String NETHER_STRING = "minecraft:the_nether";
    private final String END_STRING = "minecraft:the_end";
    private final String COMPASS_TRACKER_ID = "modded";

    private boolean active = false;

    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("trackme").executes(context -> {
                if(!active) {
                    trackedPlayer = context.getSource().getPlayer();
                    server = context.getSource().getMinecraftServer();

                    GameMessageS2CPacket messagePacket = new GameMessageS2CPacket(new LiteralText("Tracking " + trackedPlayer.getEntityName()), CHAT, Util.NIL_UUID);
                    server.getPlayerManager().sendToAll(messagePacket);

                    giveCompass();
                    active = true;
                } else {
                    GameMessageS2CPacket messagePacket = new GameMessageS2CPacket(new LiteralText("Stop tracking " + trackedPlayer.getEntityName()), CHAT, Util.NIL_UUID);
                    server.getPlayerManager().sendToAll(messagePacket);
                    active = false;
                }
                return 1;
            }));
        });
    }

    public void onTick() {
        giveCompass();
        updatePos();
        updateCompass();
    }

    private void giveCompass() {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            if (!player.equals(trackedPlayer) && !hasCompass(player)) {
                ItemStack compass = new ItemStack(Items.COMPASS);
                CompoundTag tag = new CompoundTag();
                CompoundTag tagPos = new CompoundTag();
                tagPos.putInt("X", (int) trackedPlayer.getX());
                tagPos.putInt("Y", (int) trackedPlayer.getY());
                tagPos.putInt("Z", (int) trackedPlayer.getZ());
                tag.putBoolean(COMPASS_TRACKER_ID, true);
                tag.putString("LodestoneDimension", OVERWORLD_STRING);
                tag.put("LodestonePos", tagPos);
                compass.setTag(tag);
                player.giveItemStack(compass);
            }
        });
    }

    private boolean hasCompass(ServerPlayerEntity player) {
        if (!player.equals(trackedPlayer)) {
            for (ItemStack item : player.inventory.main) {
                if (item.getItem().equals(Items.COMPASS)) {
                    if (item.getTag() != null && item.getTag().contains(COMPASS_TRACKER_ID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updatePos() {
        if (trackedPlayer == null) return;

        String dimentionString = getDimentionString(trackedPlayer);

        if (dimentionString == null) return;

        switch (dimentionString) {
            case OVERWORLD_STRING:
                lastOverworldPos = new BlockPos(trackedPlayer.getBlockPos());
                break;
            case NETHER_STRING:
                lastNetherPos = new BlockPos(trackedPlayer.getBlockPos());
                break;
            case END_STRING:
                lastEndPos = new BlockPos(trackedPlayer.getBlockPos());
                break;
        }
    }

    private void updateCompass() {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            if (!player.equals(trackedPlayer)) {
                player.inventory.main.forEach(item -> {
                    if (item.getItem().equals(Items.COMPASS)) {
                        CompoundTag tag = item.getTag();
                        if (tag != null && item.getTag().contains(COMPASS_TRACKER_ID)) {
                            String trackedPlayerDimention = getDimentionString(trackedPlayer);
                            String playerDimention = getDimentionString(player);

                            if (trackedPlayerDimention == null || playerDimention == null) return;

                            CompoundTag tagPos = tag.getCompound("LodestonePos");

                            switch (playerDimention) {
                                case OVERWORLD_STRING:
                                    tag.putString("LodestoneDimension", OVERWORLD_STRING);
                                    tagPos.putInt("X", lastOverworldPos.getX());
                                    tagPos.putInt("Y", lastOverworldPos.getY());
                                    tagPos.putInt("Z", lastOverworldPos.getZ());
                                    tag.put("LodestonePos", tagPos);
                                    break;
                                case NETHER_STRING:
                                    tag.putString("LodestoneDimension", NETHER_STRING);
                                    tagPos.putInt("X", lastNetherPos.getX());
                                    tagPos.putInt("Y", lastNetherPos.getY());
                                    tagPos.putInt("Z", lastNetherPos.getZ());
                                    tag.put("LodestonePos", tagPos);
                                    break;
                                case END_STRING:
                                    tag.putString("LodestoneDimension", END_STRING);
                                    tagPos.putInt("X", lastEndPos.getX());
                                    tagPos.putInt("Y", lastEndPos.getY());
                                    tagPos.putInt("Z", lastEndPos.getZ());
                                    tag.put("LodestonePos", tagPos);
                                    break;
                            }
                        }
                    }
                });
            }
        });
    }

    private String getDimentionString(ServerPlayerEntity player) {
        DimensionType dimensionType = player.getEntityWorld().getDimension();
        if (dimensionType == null) return null;

        if (dimensionType.hasSkyLight() == true && dimensionType.hasCeiling() == false) {
            return OVERWORLD_STRING;
        } else if (dimensionType.hasSkyLight() == false && dimensionType.hasCeiling() == true) {
            return NETHER_STRING;
        } else if (dimensionType.hasSkyLight() == false && dimensionType.hasCeiling() == false) {
            return END_STRING;
        }
        return null;
    }

    public boolean isActive() {
        return active;
    }
}
