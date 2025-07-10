package com.skibidicausa.subtitlemanager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class SubtitleNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SubtitleManagerMod.MOD_ID, "subtitle_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, SubtitlePacket.class,
                SubtitlePacket::encode, SubtitlePacket::decode, SubtitlePacket::handle);
        CHANNEL.registerMessage(packetId++, ClearSubtitlesPacket.class,
                ClearSubtitlesPacket::encode, ClearSubtitlesPacket::decode, ClearSubtitlesPacket::handle);
    }

    public static void sendSubtitlePacket(ServerPlayer player, String text, int duration) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SubtitlePacket(text, duration));
    }

    public static void sendClearPacket(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClearSubtitlesPacket());
    }

    public static class SubtitlePacket {
        private final String text;
        private final int duration;

        public SubtitlePacket(String text, int duration) {
            this.text = text;
            this.duration = duration;
        }

        public static void encode(SubtitlePacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.text, 32767);
            buffer.writeVarInt(packet.duration);
        }

        public static SubtitlePacket decode(FriendlyByteBuf buffer) {
            return new SubtitlePacket(buffer.readUtf(32767), buffer.readVarInt());
        }

        public static void handle(SubtitlePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                SubtitleManager.getInstance().addSubtitle("@client", packet.text, packet.duration);
            });
            context.setPacketHandled(true);
        }
    }

    public static class ClearSubtitlesPacket {

        public ClearSubtitlesPacket() {
        }

        public static void encode(ClearSubtitlesPacket packet, FriendlyByteBuf buffer) {
        }

        public static ClearSubtitlesPacket decode(FriendlyByteBuf buffer) {
            return new ClearSubtitlesPacket();
        }

        public static void handle(ClearSubtitlesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                SubtitleManager.getInstance().clearAll();
            });
            context.setPacketHandled(true);
        }
    }
}