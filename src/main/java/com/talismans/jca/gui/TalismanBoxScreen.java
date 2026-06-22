package com.talismans.jca.gui;

import com.talismans.jca.item.TalismanItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class TalismanBoxScreen extends AbstractContainerScreen<TalismanBoxMenu> {

    public TalismanBoxScreen(TalismanBoxMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.inventoryLabelX = -9999;
        this.inventoryLabelY = -9999;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xCC202020);
        g.renderOutline(x, y, imageWidth, imageHeight, 0xFFFFAA00);

        // 12格背景色
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                int sx = x + 20 + c * 24;
                int sy = y + 18 + r * 24;
                boolean highlight = mouseX >= sx && mouseX < sx + 20 && mouseY >= sy && mouseY < sy + 20;
                g.fill(sx, sy, sx + 20, sy + 20, highlight ? 0x66333344 : 0x33000000);
            }
        }

        // 玩家快捷栏背景框+分隔线
        int sepY = y + 92;
        g.fill(x + 4, sepY, x + imageWidth - 4, sepY + 1, 0x66AAAAAA);
        for (int c = 0; c < 9; c++) {
            int sx = x + 8 + c * 18;
            int sy = y + 100;
            boolean highlight = mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16;
            g.fill(sx, sy, sx + 16, sy + 16, highlight ? 0x66333344 : 0x22000000);
        }

        // 渲染已存入的符咒文字
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                ItemStack item = menu.slots.get(r * 4 + c).getItem();
                if (!item.isEmpty() && item.getItem() instanceof TalismanItem t) {
                    int sx = x + 20 + c * 24;
                    int sy = y + 18 + r * 24;
                    String name = t.getTalismanType().getDisplayName().substring(0, 1);
                    int color = switch (t.getTalismanType()) {
                        case RAT -> 0xFFC0C0C0;
                        case OX -> 0xFF8B0000;
                        case TIGER -> 0xFFFFD700;
                        case RABBIT -> 0xFF00CED1;
                        case DRAGON -> 0xFFFF6600;
                        case SNAKE -> 0xFF32CD32;
                        case HORSE -> 0xFFF5F5F5;
                        case SHEEP -> 0xFFBA55D3;
                        case MONKEY -> 0xFF4169E1;
                        case ROOSTER -> 0xFFFF4500;
                        case DOG -> 0xFF8B008B;
                        case PIG -> 0xFF008080;
                    };
                    g.drawCenteredString(font, name, sx + 10, sy + 5, color);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFAA00, false);
    }
}
