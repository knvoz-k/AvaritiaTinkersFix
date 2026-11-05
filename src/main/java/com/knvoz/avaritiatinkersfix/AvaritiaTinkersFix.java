package com.knvoz.avaritiatinkersfix;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import slimeknights.tconstruct.tools.common.tileentity.TileToolForge;
import slimeknights.tconstruct.tools.common.tileentity.TileToolStation;

/**
 * Client-side addon that makes Avaritia's halo items render correctly when they are displayed on
 * Tinkers' Antique tool station / tool forge table tops, including the halo and pulse effects.
 *
 * <p>Why this is needed: Avaritia's halo item models are CodeChickenLib {@code IItemRenderer}s which
 * report {@code isBuiltInRenderer() == true} and provide no quads. Tinkers' Antique replaces such
 * models with the vanilla missing model when baking the table top model (see
 * {@code ModelHelper.getBakedModelForItem}), which is what produced the magenta/black cube.</p>
 *
 * <p>The model swap itself is a mixin on {@code ModelHelper}, registered by
 * {@link com.knvoz.avaritiatinkersfix.core.AvaritiaTinkersFixCorePlugin}; this class only binds the
 * TESR that draws the animated halo and pulse on top of the baked item.</p>
 */
@Mod(
    modid = AvaritiaTinkersFix.MOD_ID,
    name = "Avaritia Tinkers Fix",
    version = "1.0.0",
    clientSideOnly = true,
    dependencies = "required-after:mixinbooter;required-after:tconstruct;required-after:avaritia;required-after:codechickenlib"
)
public class AvaritiaTinkersFix {

    public static final String MOD_ID = "avaritiatinkersfix";

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void init(FMLInitializationEvent event) {
        HaloTableRenderer renderer = new HaloTableRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileToolStation.class, renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileToolForge.class, (TileEntitySpecialRenderer) renderer);
    }
}
