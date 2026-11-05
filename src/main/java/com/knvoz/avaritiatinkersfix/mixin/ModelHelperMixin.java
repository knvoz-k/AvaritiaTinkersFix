package com.knvoz.avaritiatinkersfix.mixin;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.knvoz.avaritiatinkersfix.HaloTableModels;

import slimeknights.tconstruct.library.client.model.ModelHelper;

/**
 * Lets Tinkers' Antique bake Avaritia's CodeChickenLib-rendered items onto table tops.
 *
 * <p>{@code ModelHelper.getBakedModelForItem} replaces every model that reports
 * {@code isBuiltInRenderer() == true} with the vanilla missing model, which is what produced the
 * magenta/black cube. For items that are backed by Avaritia's
 * {@code WrappedItemRenderer} we hand it a model that exposes the real geometry instead.</p>
 *
 * <p>{@code remap = false}: the target is a mod method, not a Minecraft name, and this mod ships no
 * refmap.</p>
 */
@Mixin(value = ModelHelper.class, remap = false)
public abstract class ModelHelperMixin {

    @Inject(method = "getBakedModelForItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void avaritiatinkersfix$useBakeableModel(ItemStack stack, World world, EntityLivingBase entity,
                                                            CallbackInfoReturnable<IBakedModel> cir) {
        IBakedModel model = HaloTableModels.forTableBaking(stack);
        if (model != null) {
            cir.setReturnValue(model);
        }
    }
}
