package eu.manhuntcompass;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

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
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(literal("trackme").executes(context -> {
            if (!active) {
                trackedPlayer = context.getSource().getPlayer();
                server = context.getSource().getMinecraftServer();

                GameMessageS2CPacket messagePacket = new GameMessageS2CPacket(new LiteralText("Tracking " + trackedPlayer.getEntityName()), CHAT, Util.NIL_UUID);
                server.getPlayerManager().sendToAll(messagePacket);

                updatePos();
                giveCompass();
                active = true;
            } else {
                GameMessageS2CPacket messagePacket = new GameMessageS2CPacket(new LiteralText("Stop tracking " + trackedPlayer.getEntityName()), CHAT, Util.NIL_UUID);
                server.getPlayerManager().sendToAll(messagePacket);
                active = false;
            }
            return 1;
        })));
    }

    public void onPlayerMove() {
        giveCompass();
        updatePos();
        updateCompass();
    }

    private void giveCompass() {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            if (!player.equals(trackedPlayer) && !hasCompass(player)) {
                ItemStack compass = new ItemStack(Items.COMPASS);
                CompoundTag tag = new CompoundTag();
                tag.putBoolean(COMPASS_TRACKER_ID, true);
                tag.putBoolean("LodestoneTracked", true);
                tag.putString("LodestoneDimension", OVERWORLD_STRING);
                if(lastOverworldPos != null) {
                    tag.put("LodestonePos", NbtHelper.fromBlockPos(lastOverworldPos));
                }
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

        CompoundTag trackedPlayerTag = new CompoundTag();
        trackedPlayer.toTag(trackedPlayerTag);
        String dimentionString = trackedPlayerTag.getString("Dimension");

        if (dimentionString == null) return;

        switch (dimentionString) {
            case OVERWORLD_STRING:
                lastOverworldPos = trackedPlayer.getBlockPos();
                break;
            case NETHER_STRING:
                lastNetherPos = trackedPlayer.getBlockPos();
                break;
            case END_STRING:
                lastEndPos = trackedPlayer.getBlockPos();
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
                            CompoundTag playerTag = new CompoundTag();
                            player.toTag(playerTag);
                            String playerDimention = playerTag.getString("Dimension");

                            tag.putBoolean("LodestoneTracked", true);
                            tag.putString("LodestoneDimension", playerDimention);
                            switch (playerDimention) {
                                case OVERWORLD_STRING:
                                    if (lastOverworldPos != null) {
                                        tag.put("LodestonePos", NbtHelper.fromBlockPos(lastOverworldPos));
                                    }
                                    break;
                                case NETHER_STRING:
                                    if (lastNetherPos != null) {
                                        tag.put("LodestonePos", NbtHelper.fromBlockPos(lastNetherPos));
                                    }
                                    break;
                                case END_STRING:
                                    if (lastEndPos != null) {
                                        tag.put("LodestonePos", NbtHelper.fromBlockPos(lastEndPos));
                                    }
                                    break;
                            }
                        }
                    }
                });
            }
        });
    }

    public BlockPos getLastOverworldPos() {
        return lastOverworldPos;
    }

    public BlockPos getLastNetherPos() {
        return lastNetherPos;
    }

    public BlockPos getLastEndPos() {
        return lastEndPos;
    }

    public ServerPlayerEntity getTrackedPlayer() {
        return trackedPlayer;
    }

    public boolean isActive() {
        return active;
    }
}
