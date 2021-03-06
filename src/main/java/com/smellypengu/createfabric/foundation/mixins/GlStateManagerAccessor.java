package com.smellypengu.createfabric.foundation.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
    @Accessor("FOG")
    static GlStateManager.FogState getFog() {
        throw new AssertionError();
    }
}