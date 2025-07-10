package com.skibidicausa.subtitlemanager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skibidicausa.subtitlemanager.SubtitleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(Gui.class)
public class SubtitleRenderMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCustomSubtitles(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;

        SubtitleManager manager = SubtitleManager.getInstance();

        String playerName = minecraft.player.getName().getString();
        ConcurrentLinkedQueue<SubtitleManager.SubtitleData> playerSubtitles = manager.getSubtitlesForPlayer(playerName);
        ConcurrentLinkedQueue<SubtitleManager.SubtitleData> globalSubtitles = manager.getGlobalSubtitles();
        ConcurrentLinkedQueue<SubtitleManager.SubtitleData> clientSubtitles = manager.getSubtitlesForPlayer("@client");

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int baseY = screenHeight - 120;
        int currentY = baseY;
        int lineHeight = 16;

        PoseStack poseStack = guiGraphics.pose();

        for (SubtitleManager.SubtitleData subtitle : clientSubtitles) {
            renderSubtitle(guiGraphics, poseStack, subtitle, screenWidth, currentY, minecraft);
            currentY -= lineHeight;
        }

        for (SubtitleManager.SubtitleData subtitle : globalSubtitles) {
            renderSubtitle(guiGraphics, poseStack, subtitle, screenWidth, currentY, minecraft);
            currentY -= lineHeight;
        }

        for (SubtitleManager.SubtitleData subtitle : playerSubtitles) {
            renderSubtitle(guiGraphics, poseStack, subtitle, screenWidth, currentY, minecraft);
            currentY -= lineHeight;
        }

        manager.tick();
    }

    private void renderSubtitle(GuiGraphics guiGraphics, PoseStack poseStack, SubtitleManager.SubtitleData subtitle,
                                int screenWidth, int y, Minecraft minecraft) {
        Component text = subtitle.getText();
        int textWidth = minecraft.font.width(text);
        int x = (screenWidth - textWidth) / 2;

        float alpha = subtitle.getAlpha();
        float scale = subtitle.getScale();

        int alphaInt = (int)(alpha * 255);

        poseStack.pushPose();
        poseStack.translate(x + textWidth / 2f, y + 5, 0);
        poseStack.scale(scale, scale, 1.0f);
        poseStack.translate(-(textWidth / 2f), -5, 0);

        int textColor = 0xFFFFFF | (alphaInt << 24);

        int backgroundAlpha = Math.min(180, (int)(alpha * 180));
        int backgroundColor = 0x000000 | (backgroundAlpha << 24);

        guiGraphics.fill((int)(-textWidth / 2f) - 4, -2, (int)(textWidth / 2f) + 4, 12, backgroundColor);

        int borderAlpha = Math.min(100, (int)(alpha * 100));
        int borderColor = 0x555555 | (borderAlpha << 24);

        guiGraphics.fill((int)(-textWidth / 2f) - 4, -2, (int)(-textWidth / 2f) - 3, 12, borderColor);
        guiGraphics.fill((int)(textWidth / 2f) + 3, -2, (int)(textWidth / 2f) + 4, 12, borderColor);
        guiGraphics.fill((int)(-textWidth / 2f) - 4, -2, (int)(textWidth / 2f) + 4, -1, borderColor);
        guiGraphics.fill((int)(-textWidth / 2f) - 4, 11, (int)(textWidth / 2f) + 4, 12, borderColor);

        guiGraphics.drawString(minecraft.font, text, (int)(-textWidth / 2f), 0, textColor, true);

        poseStack.popPose();
    }
}