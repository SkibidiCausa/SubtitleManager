package com.skibidicausa.subtitlemanager;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SubtitleManager {
    private static final SubtitleManager INSTANCE = new SubtitleManager();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<SubtitleData>> playerSubtitles = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<SubtitleData> globalSubtitles = new ConcurrentLinkedQueue<>();

    public static SubtitleManager getInstance() {
        return INSTANCE;
    }

    public void addSubtitle(String playerName, String text, int duration) {
        MutableComponent formattedText = Component.literal("");
        String[] parts = text.split("&");

        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                formattedText.append(Component.literal(parts[i]));
            } else if (parts[i].length() > 0) {
                char colorCode = parts[i].charAt(0);
                String content = parts[i].substring(1);

                ChatFormatting format = getFormatFromCode(colorCode);
                if (format != null) {
                    formattedText.append(Component.literal(content).withStyle(format));
                } else {
                    formattedText.append(Component.literal("&" + parts[i]));
                }
            }
        }

        SubtitleData subtitle = new SubtitleData(formattedText, duration);

        if (playerName.equals("@all") || playerName.equals("@a")) {
            globalSubtitles.offer(subtitle);
        } else {
            playerSubtitles.computeIfAbsent(playerName, k -> new ConcurrentLinkedQueue<>()).offer(subtitle);
        }
    }

    private ChatFormatting getFormatFromCode(char code) {
        switch (code) {
            case '0': return ChatFormatting.BLACK;
            case '1': return ChatFormatting.DARK_BLUE;
            case '2': return ChatFormatting.DARK_GREEN;
            case '3': return ChatFormatting.DARK_AQUA;
            case '4': return ChatFormatting.DARK_RED;
            case '5': return ChatFormatting.DARK_PURPLE;
            case '6': return ChatFormatting.GOLD;
            case '7': return ChatFormatting.GRAY;
            case '8': return ChatFormatting.DARK_GRAY;
            case '9': return ChatFormatting.BLUE;
            case 'a': return ChatFormatting.GREEN;
            case 'b': return ChatFormatting.AQUA;
            case 'c': return ChatFormatting.RED;
            case 'd': return ChatFormatting.LIGHT_PURPLE;
            case 'e': return ChatFormatting.YELLOW;
            case 'f': return ChatFormatting.WHITE;
            case 'l': return ChatFormatting.BOLD;
            case 'o': return ChatFormatting.ITALIC;
            case 'n': return ChatFormatting.UNDERLINE;
            case 'm': return ChatFormatting.STRIKETHROUGH;
            case 'k': return ChatFormatting.OBFUSCATED;
            case 'r': return ChatFormatting.RESET;
            default: return null;
        }
    }

    public ConcurrentLinkedQueue<SubtitleData> getSubtitlesForPlayer(String playerName) {
        return playerSubtitles.getOrDefault(playerName, new ConcurrentLinkedQueue<>());
    }

    public ConcurrentLinkedQueue<SubtitleData> getGlobalSubtitles() {
        return globalSubtitles;
    }

    public void tick() {
        globalSubtitles.removeIf(subtitle -> {
            subtitle.tick();
            return subtitle.isExpired();
        });

        playerSubtitles.values().forEach(queue -> {
            queue.removeIf(subtitle -> {
                subtitle.tick();
                return subtitle.isExpired();
            });
        });

        playerSubtitles.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public void clearAll() {
        globalSubtitles.clear();
        playerSubtitles.clear();
    }

    public static class SubtitleData {
        private final Component text;
        private int remainingTicks;
        private final int maxTicks;
        private final long startTime;

        public SubtitleData(Component text, int duration) {
            this.text = text;
            this.remainingTicks = duration;
            this.maxTicks = duration;
            this.startTime = System.currentTimeMillis();
        }

        public Component getText() {
            return text;
        }

        public int getRemainingTicks() {
            return remainingTicks;
        }

        public int getMaxTicks() {
            return maxTicks;
        }

        public float getAlpha() {
            if (remainingTicks > 40) {
                return 1.0f;
            } else if (remainingTicks > 20) {
                return 0.8f;
            } else {
                return Math.max(0.0f, remainingTicks / 20.0f);
            }
        }

        public float getScale() {
            if (remainingTicks > maxTicks - 10) {
                float progress = (maxTicks - remainingTicks) / 10.0f;
                return 0.5f + (0.5f * progress);
            }
            return 1.0f;
        }

        public void tick() {
            remainingTicks--;
        }

        public boolean isExpired() {
            return remainingTicks <= 0;
        }
    }
}