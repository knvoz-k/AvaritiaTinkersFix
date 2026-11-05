package com.knvoz.avaritiatinkersfix.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import zone.rong.mixinbooter.ILateMixinLoader;
import zone.rong.mixinbooter.MixinLoader;

/**
 * Core plugin that only exists to hand the mixin config to MixinBooter.
 *
 * <p>The mixin has to be in place before Tinkers' Antique loads its client model helper, so it
 * cannot be queued from the {@code @Mod} constructor.</p>
 */
@MixinLoader
public class AvaritiaTinkersFixCorePlugin implements IFMLLoadingPlugin, ILateMixinLoader {

    public static final String MIXIN_CONFIG = "avaritiatinkersfix.mixins.json";

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(MIXIN_CONFIG);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No-op.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
