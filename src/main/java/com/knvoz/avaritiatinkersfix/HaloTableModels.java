package com.knvoz.avaritiatinkersfix;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import morph.avaritia.client.render.item.WrappedItemRenderer;

import slimeknights.tconstruct.shared.client.BakedColoredItemModel;

/**
 * Turns a CodeChickenLib item renderer into a model Tinkers' Antique can bake into a table top.
 *
 * <p>CCL's {@code IItemRenderer} reports {@code isBuiltInRenderer() == true} and returns an empty
 * quad list, so Tinkers' {@code ModelHelper.getBakedModelForItem} swaps it for the vanilla missing
 * model - that is where the magenta/black cube on the table comes from.</p>
 *
 * <p>This is only used from the {@code ModelHelper} mixin, so the model registry keeps the original
 * renderer. JEI, CodeChickenLib and everything else keep seeing the untouched model.</p>
 */
@SideOnly(Side.CLIENT)
public final class HaloTableModels {

    private static Field wrappedField;
    private static boolean wrappedFieldUnavailable;

    private HaloTableModels() {
    }

    /**
     * @return a model exposing the real item geometry for Tinkers' table top baking, wrapped the
     *         same way Tinkers wraps every other item, or {@code null} when the stack does not use
     *         an Avaritia renderer.
     */
    public static IBakedModel forTableBaking(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        IBakedModel model;
        try {
            model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        }
        catch (Throwable t) {
            return null;
        }

        if (!(model instanceof WrappedItemRenderer)) {
            return null;
        }

        IBakedModel wrapped = getWrappedModel(model);
        if (wrapped == null || wrapped == model) {
            return null;
        }

        return new BakedColoredItemModel(stack, new HaloTableModel(wrapped));
    }

    private static IBakedModel getWrappedModel(IBakedModel model) {
        if (wrappedFieldUnavailable) {
            return null;
        }
        try {
            if (wrappedField == null) {
                Field field = WrappedItemRenderer.class.getDeclaredField("wrapped");
                field.setAccessible(true);
                wrappedField = field;
            }
            Object value = wrappedField.get(model);
            return value instanceof IBakedModel ? (IBakedModel) value : null;
        }
        catch (Throwable t) {
            // Give up quietly: Tinkers then falls back to its own behaviour.
            wrappedFieldUnavailable = true;
            return null;
        }
    }
}
