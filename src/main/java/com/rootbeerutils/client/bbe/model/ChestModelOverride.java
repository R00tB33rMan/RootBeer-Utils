package com.rootbeerutils.client.bbe.model;

import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.config.EnumTypes;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.chest.ChestModel;

public class ChestModelOverride extends ChestModel {

    public ChestModelOverride(ModelPart root) {
        super(root);

        if (ConfigCache.updateType != EnumTypes.UpdateSchedulerType.SMART.ordinal()) {
            root.getChild("bottom").skipDraw = true;
            root.getChild("bottom").visible = false;
        }
    }
}
