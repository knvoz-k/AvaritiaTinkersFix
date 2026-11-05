package com.knvoz.avaritiatinkersfix;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Exposes the geometry of an Avaritia item model to Tinkers' Antique.
 *
 * <p>The wrapped model is the real item model that sits behind Avaritia's
 * {@link morph.avaritia.client.render.item.WrappedItemRenderer}; it is only reachable through its
 * protected {@code wrapped} field. The only behaviour this changes is
 * {@link #isBuiltInRenderer()}: returning {@code false} stops Tinkers from substituting the missing
 * model, so the item gets baked into the table top like any other item.</p>
 *
 * <p>Instances are never put into the model registry - see {@link HaloTableModels}.</p>
 */
@SideOnly(Side.CLIENT)
public class HaloTableModel implements IBakedModel {

    private final IBakedModel wrapped;

    public HaloTableModel(IBakedModel wrapped) {
        this.wrapped = wrapped;
    }

    public IBakedModel getWrappedModel() {
        return wrapped;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return wrapped.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return wrapped.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        // Deliberately false: Tinkers replaces built-in-renderer models with the missing model.
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return wrapped.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return wrapped.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return wrapped.getOverrides();
    }
}
