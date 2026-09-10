package com.gothbreach.client.event.events;

import com.gothbreach.client.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class RenderWorldEvent extends Event {
    private final MatrixStack matrices;
    public RenderWorldEvent(MatrixStack matrices) { this.matrices = matrices; }
    public MatrixStack getMatrices() { return matrices; }
}
