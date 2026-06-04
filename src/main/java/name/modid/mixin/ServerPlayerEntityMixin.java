package name.modid.mixin;

import name.modid.nutrition.NutritionManager;
import name.modid.nutrition.NutritionState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Unique
    private ServerPlayerEntity getSelf() {
        return (ServerPlayerEntity) (Object) this;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void dontEatThat$readNutrition(NbtCompound nbt, CallbackInfo ci) {
        NutritionManager.load(getSelf(), NutritionState.fromPlayerNbt(nbt));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void dontEatThat$writeNutrition(NbtCompound nbt, CallbackInfo ci) {
        NutritionState.writeToPlayerNbt(nbt, NutritionManager.get(getSelf()));
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void dontEatThat$onDamage(net.minecraft.entity.damage.DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (NutritionManager.tryDodge(getSelf(), source)) {
            cir.setReturnValue(false);
            return;
        }
        NutritionManager.onDamage(getSelf(), source, amount);
    }
}
