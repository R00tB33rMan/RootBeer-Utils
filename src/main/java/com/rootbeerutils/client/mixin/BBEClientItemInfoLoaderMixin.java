package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.config.ConfigCache;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ClientItemInfoLoader.class)
public class BBEClientItemInfoLoaderMixin {

    @Unique private static final Identifier CHEST_ITEM_ID = Identifier.withDefaultNamespace("chest");
    @Unique private static final Identifier TRAPPED_CHEST_ITEM_ID = Identifier.withDefaultNamespace("trapped_chest");

    @Inject(method = "scheduleLoad", at = @At("RETURN"), cancellable = true)
    private static void rewriteChestItemAssets(ResourceManager manager, Executor executor,
                                               CallbackInfoReturnable<CompletableFuture<ClientItemInfoLoader.LoadedClientInfos>> cir) {
        // Patch unconditionally now — the rewrite function itself decides per-model whether
        // anything needs to change based on current config + the model's existing texture.
        CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> original = cir.getReturnValue();
        cir.setReturnValue(original.thenApply(BBEClientItemInfoLoaderMixin::rewriteChestItems));
    }

    @Unique
    private static ClientItemInfoLoader.LoadedClientInfos rewriteChestItems(ClientItemInfoLoader.LoadedClientInfos loaded) {
        Map<Identifier, ClientItem> map = new HashMap<>(loaded.contents());

        // Decide direction once, applies to both chest types.
        boolean wantChristmas = ConfigCache.christmasChests;

        if (wantChristmas) {
            // regular → Christmas
            patchOne(map, CHEST_ITEM_ID,         /* from */ ChestSpecialRenderer.REGULAR.single(), /* to */ ChestSpecialRenderer.CHRISTMAS.single());
            patchOne(map, TRAPPED_CHEST_ITEM_ID, /* from */ ChestSpecialRenderer.TRAPPED.single(), /* to */ ChestSpecialRenderer.CHRISTMAS.single());
        } else {
            // Christmas → regular (e.g., during the Dec/Jan window when vanilla auto-bakes Christmas)
            patchOne(map, CHEST_ITEM_ID,         /* from */ ChestSpecialRenderer.CHRISTMAS.single(), /* to */ ChestSpecialRenderer.REGULAR.single());
            patchOne(map, TRAPPED_CHEST_ITEM_ID, /* from */ ChestSpecialRenderer.CHRISTMAS.single(), /* to */ ChestSpecialRenderer.TRAPPED.single());
        }

        return new ClientItemInfoLoader.LoadedClientInfos(Map.copyOf(map));
    }

    @Unique
    private static void patchOne(Map<Identifier, ClientItem> map, Identifier id, Identifier fromTexture, Identifier toTexture) {
        ClientItem existing = map.get(id);
        if (existing == null) {
            return;
        }

        ItemModel.Unbaked patchedModel = patchModel(existing.model(), fromTexture, toTexture);
        if (patchedModel != existing.model()) {
            map.put(id, new ClientItem(patchedModel, existing.properties(), existing.registrySwapper()));
        }
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ItemModel.Unbaked patchModel(ItemModel.Unbaked model, Identifier fromTexture, Identifier toTexture) {
        if (model instanceof SpecialModelWrapper.Unbaked(Identifier base, Optional<Transformation> transformation, SpecialModelRenderer.Unbaked special)) {
            if (special instanceof ChestSpecialRenderer.Unbaked(Identifier tex, float openness, ChestType type)) {
                // Match either the exact "from" texture or — for the Christmas → regular direction —
                // any path containing "Christmas" (catches case variants in modded packs).
                boolean matchesFrom = tex.equals(fromTexture)
                        || (fromTexture.equals(ChestSpecialRenderer.CHRISTMAS.single()) && tex.getPath().contains("Christmas"));
                if (matchesFrom) {
                    ChestSpecialRenderer.Unbaked replaced = new ChestSpecialRenderer.Unbaked(toTexture, openness, type);
                    return new SpecialModelWrapper.Unbaked(base, transformation, replaced);
                }
            }

            return model;
        }

        if (model instanceof SelectItemModel.Unbaked(Optional<Transformation> transformation, SelectItemModel.UnbakedSwitch<?, ?> sw, Optional<ItemModel.Unbaked> oldFallback)) {
            List<SelectItemModel.SwitchCase> cases = (List) sw.cases();

            boolean changed = false;
            List<SelectItemModel.SwitchCase> newCases = new ArrayList<>(cases.size());
            for (SelectItemModel.SwitchCase sc : cases) {
                ItemModel.Unbaked child = sc.model();
                ItemModel.Unbaked patchedChild = patchModel(child, fromTexture, toTexture);
                if (patchedChild != child) {
                    changed = true;
                }

                newCases.add(patchedChild == child ? sc : new SelectItemModel.SwitchCase(sc.values(), patchedChild));
            }

            Optional<ItemModel.Unbaked> newFallback = oldFallback.map(fb -> patchModel(fb, fromTexture, toTexture));
            if (!oldFallback.equals(newFallback)) {
                changed = true;
            }

            if (!changed) {
                return model;
            }

            SelectItemModel.UnbakedSwitch newSw = new SelectItemModel.UnbakedSwitch(sw.property(), newCases);
            return new SelectItemModel.Unbaked(transformation, newSw, newFallback);
        }

        return model;
    }
}
