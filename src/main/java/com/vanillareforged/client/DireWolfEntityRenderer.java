package com.vanillareforged.client;

import com.vanillareforged.entity.DireWolfEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DireWolfEntityRenderer extends WolfEntityRenderer {

    public DireWolfEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        // Uses EntityModelLayers.WOLF via parent WolfEntityRenderer
    }

    @Override
    public void updateRenderState(net.minecraft.entity.passive.WolfEntity entity, WolfEntityRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        if (entity instanceof DireWolfEntity direWolf) {
            state.texture = direWolf.getTextureForState();
        }
    }

    @Override
    public Identifier getTexture(WolfEntityRenderState state) {
        return state.texture != null ? state.texture : DireWolfEntity.REGULAR_WILD_TEXTURE;
    }
}
