package com.rootbeerutils.client.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.rootbeerutils.main.appleskin.helpers.ExhaustionHelper;

@Mixin(FoodData.class)
public class AppleSkinFoodDataMixin implements ExhaustionHelper.ExhaustionManipulator
{
    @Shadow
    private float exhaustionLevel;

    @Override
    public void setExhaustion(float value)
    {
        this.exhaustionLevel = value;
    }

    @Override
    public float getExhaustion()
    {
        return this.exhaustionLevel;
    }
}
