package com.knvoz.avaritiatinkersfix;

import java.util.Random;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import morph.avaritia.api.IHaloRenderItem;
import morph.avaritia.client.render.item.WrappedItemRenderer;
import slimeknights.tconstruct.shared.block.BlockTable;
import slimeknights.tconstruct.shared.block.PropertyTableItem;
import slimeknights.tconstruct.tools.common.tileentity.TileToolStation;

/**
 * Draws Avaritia's halo and pulse effects on top of the item models that Tinkers' Antique bakes
 * into its tool station / tool forge table tops.
 *
 * <p>The item itself is still drawn by Tinkers (as baked block model geometry, so lighting and
 * occlusion are correct). This renderer only adds the animated parts that cannot be baked:
 * the halo quad and the randomly scaled "pulse" duplicate of the item.</p>
 *
 * <p>All positions come from Tinkers' own {@link PropertyTableItem.TableItem}s, which are read
 * from the block's extended state. This guarantees the overlay lines up with the baked item.</p>
 */
@SideOnly(Side.CLIENT)
public class HaloTableRenderer extends TileEntitySpecialRenderer<TileToolStation> {

    /**
     * Model space Z of the generated item model. Forge bakes {@code builtin/generated} items as a
     * 1/16 thick slab centred on z = 0.5 (front at 7.5/16, back at 8.5/16), and Tinkers rotates the
     * model so that +Z points up. Drawing the halo in the middle of the slab keeps it just under
     * the visible face (so the item occludes the middle) while still being above the table top.
     */
    private static final float HALO_Z = 0.5F;

    /** Pushes the pulse duplicate just under the item's top face so it only shows around the edges. */
    private static final float PULSE_Z_OFFSET = -0.01F;

    private final Random random = new Random();

    @Override
    public void render(TileToolStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        World world = te.getWorld();
        if (world == null) {
            return;
        }
        BlockPos pos = te.getPos();
        if (!world.isBlockLoaded(pos)) {
            return;
        }

        IBlockState state = world.getBlockState(pos);
        IBlockState extendedState = state.getBlock().getExtendedState(state, world, pos);
        if (!(extendedState instanceof IExtendedBlockState)) {
            return;
        }
        IExtendedBlockState extended = (IExtendedBlockState) extendedState;

        // Tinkers only puts items in here when "renderInventoryInWorld" is enabled, and the model
        // being non-null also means the table item display is active.
        PropertyTableItem.TableItems items = extended.getValue(BlockTable.INVENTORY);
        if (items == null || items.items.isEmpty()) {
            return;
        }

        boolean hasHaloItem = false;
        for (PropertyTableItem.TableItem item : items.items) {
            if (isHaloItem(item)) {
                hasHaloItem = true;
                break;
            }
        }
        if (!hasHaloItem) {
            return;
        }

        // PropertyUnlistedDirection implements both IProperty and IUnlistedProperty, so the cast
        // is needed to disambiguate IExtendedBlockState#getValue.
        EnumFacing facing = extended.getValue((IUnlistedProperty<EnumFacing>) BlockTable.FACING);
        if (facing == null) {
            // BakedTableModel uses SOUTH when the facing property is absent.
            facing = EnumFacing.SOUTH;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        ForgeHooksClient.multiplyCurrentGlMatrix(facingMatrix(facing));

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.color(1F, 1F, 1F, 1F);

        for (PropertyTableItem.TableItem item : items.items) {
            if (!isHaloItem(item)) {
                continue;
            }

            GlStateManager.pushMatrix();
            ForgeHooksClient.multiplyCurrentGlMatrix(itemMatrix(item));
            drawHalo(item.stack, (IHaloRenderItem) item.stack.getItem());
            drawPulse(item.stack, item.model);
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static boolean isHaloItem(PropertyTableItem.TableItem item) {
        return item != null
               && item.model != null
               && item.stack != null
               && !item.stack.isEmpty()
               && item.stack.getItem() instanceof IHaloRenderItem;
    }

    private void drawHalo(ItemStack stack, IHaloRenderItem halo) {
        if (!halo.shouldDrawHalo(stack)) {
            return;
        }
        TextureAtlasSprite sprite = halo.getHaloTexture(stack);
        if (sprite == null) {
            return;
        }

        int colour = halo.getHaloColour(stack);
        float a = (colour >> 24 & 0xFF) / 255F;
        float r = (colour >> 16 & 0xFF) / 255F;
        float g = (colour >> 8 & 0xFF) / 255F;
        float b = (colour & 0xFF) / 255F;

        double spread = halo.getHaloSize(stack) / 16D;
        double min = -spread;
        double max = 1D + spread;

        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Same quad as HaloRenderItem's GUI halo, but drawn in the item's model space with depth
        // testing enabled so the item itself covers the middle of the halo.
        GlStateManager.color(r, g, b, a);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(max, max, HALO_Z).tex(maxU, minV).endVertex();
        buffer.pos(min, max, HALO_Z).tex(minU, minV).endVertex();
        buffer.pos(min, min, HALO_Z).tex(minU, maxV).endVertex();
        buffer.pos(max, min, HALO_Z).tex(maxU, maxV).endVertex();
        tessellator.draw();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private void drawPulse(ItemStack stack, IBakedModel model) {
        IHaloRenderItem halo = (IHaloRenderItem) stack.getItem();
        if (!halo.shouldDrawPulse(stack)) {
            return;
        }

        // Random scale per frame, exactly like HaloRenderItem does in the GUI.
        float scale = (float) (random.nextDouble() * 0.15D + 0.95D);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0F);
        GlStateManager.scale(scale, scale, 1F);
        GlStateManager.translate(-0.5F, -0.5F, PULSE_Z_OFFSET);
        WrappedItemRenderer.renderModel(model, stack, 0.6F);
        GlStateManager.popMatrix();
    }

    /**
     * Reproduces the outer rotation that {@code BakedTableModel} applies through
     * {@code new TRSRBakedModel(model, facing)}.
     *
     * <p>Mantle uses {@code EnumFacing#getHorizontalIndex()} here (the S-W-N-E order), not
     * {@code getIndex()} (the D-U-N-S-W-E order). Getting this wrong rotates the item by a
     * multiple of 90 degrees around the block centre.</p>
     */
    private static Matrix4f facingMatrix(EnumFacing facing) {
        double yaw = Math.PI * (360.0D - facing.getOpposite().getHorizontalIndex() * 90.0D) / 180.0D;
        TRSRTransformation transform = new TRSRTransformation(
            null, null, null, TRSRTransformation.quatFromXYZ(0F, (float) yaw, 0F));
        return TRSRTransformation.blockCenterToCorner(transform).getMatrix();
    }

    /**
     * Reproduces {@code BakedTableModel#getModelForTableItem}:
     * {@code new TRSRBakedModel(model, x, y + 1, z, r, PI, 0, s)}.
     */
    private static Matrix4f itemMatrix(PropertyTableItem.TableItem item) {
        TRSRTransformation transform = new TRSRTransformation(
            new Vector3f(item.x, item.y + 1F, item.z),
            null,
            new Vector3f(item.s, item.s, item.s),
            TRSRTransformation.quatFromXYZ(item.r, (float) Math.PI, 0F));
        return TRSRTransformation.blockCenterToCorner(transform).getMatrix();
    }
}
