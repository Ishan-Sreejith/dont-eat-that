package name.modid.mixin;

import name.modid.nutrition.NutritionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "eatFood", at = @At("HEAD"))
    private void dontEatThat$onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient() && (Object) this instanceof ServerPlayerEntity serverPlayer) {
            NutritionManager.onFoodEaten(serverPlayer, stack);
        }
    }
}
