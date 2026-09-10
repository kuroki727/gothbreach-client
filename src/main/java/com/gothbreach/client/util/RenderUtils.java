package com.gothbreach.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {
    public static void drawBox(MatrixStack matrices, Box box, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // 12 рёбер
        // Нижняя грань
        vertex(buffer, matrices, box.minX, box.minY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.minY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.minY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.minY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.minY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.minY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.minY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.minY, box.minZ, r,g,b,a);

        // Верхняя грань
        vertex(buffer, matrices, box.minX, box.maxY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.maxY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.maxY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.maxY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.maxY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.maxY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.maxY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.maxY, box.minZ, r,g,b,a);

        // Вертикальные рёбра
        vertex(buffer, matrices, box.minX, box.minY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.maxY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.minY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.maxY, box.minZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.minY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.maxX, box.maxY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.minY, box.maxZ, r,g,b,a);
        vertex(buffer, matrices, box.minX, box.maxY, box.maxZ, r,g,b,a);

        tessellator.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void vertex(BufferBuilder buffer, MatrixStack matrices, double x, double y, double z, float r, float g, float b, float a) {
        buffer.vertex(matrices.peek().getPositionMatrix(), (float)x, (float)y, (float)z).color(r,g,b,a).next();
    }

    public static void drawLine(MatrixStack matrices, Vec3d start, Vec3d end, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrices.peek().getPositionMatrix(), (float)start.x, (float)start.y, (float)start.z).color(r,g,b,a).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), (float)end.x, (float)end.y, (float)end.z).color(r,g,b,a).next();
        tessellator.draw();
    }
}
