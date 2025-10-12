package io.github.Sorcery_Dynasties.aperioculos.mixin;

import io.github.Sorcery_Dynasties.aperioculos.api.AperiOculosAPI;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin类：拦截原版LivingEntity.hasLineOfSight()方法
 * 当配置启用时，使用Aperi Oculos的视觉系统替代原版实现
 * 这样可以复用缓存，避免重复的光线追踪计算
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * 拦截LivingEntity.hasLineOfSight()方法
     * 在方法头部注入，如果配置启用则使用Aperi Oculos的实现并取消原方法调用
     */
    @Inject(method = "hasLineOfSight", at = @At("HEAD"), cancellable = true)
    private void onHasLineOfSight(Entity target, CallbackInfoReturnable<Boolean> cir) {
        // 检查配置是否启用覆盖
        if (!Config.OVERRIDE_VANILLA_HAS_LINE_OF_SIGHT.get()) {
            return; // 保持原版行为
        }

        // 只处理LivingEntity之间的视线检查
        if (!(target instanceof LivingEntity livingTarget)) {
            return; // 对于非生物实体，保持原版行为
        }

        // 获取观察者（当前LivingEntity实例）
        LivingEntity observer = (LivingEntity) (Object) this;

        // 使用Aperi Oculos的canSee实现（带缓存）
        boolean result = AperiOculosAPI.canSee(observer, livingTarget);

        // 设置返回值并取消原方法调用
        cir.setReturnValue(result);
    }
}
