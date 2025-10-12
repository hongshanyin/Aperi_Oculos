package io.github.Sorcery_Dynasties.aperioculos.api;

import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import io.github.Sorcery_Dynasties.aperioculos.util.LineOfSightChecker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

/**
 * Aperi Oculos 公共API
 * 提供感知相关的查询方法
 */
public final class AperiOculosAPI {

    /**
     * 完整的视觉检查：距离 + FoV + 光照 + 视线 + 隐身
     */
    public static boolean canSee(LivingEntity observer, LivingEntity target) {
        if (observer == null || target == null || !observer.isAlive() || !target.isAlive() || observer.equals(target)) {
            return false;
        }
        if (isBlind(observer)) return false;

        // 隐身检查：如果目标隐身且没有发光效果，则无法被看到
        if (target.hasEffect(MobEffects.INVISIBILITY) && !target.hasEffect(MobEffects.GLOWING)) {
            io.github.Sorcery_Dynasties.aperioculos.util.PerceptionLogger.logVisionCheckFailed(
                observer, target, "Target is invisible (without glowing effect)"
            );
            return false;
        }

        // 距离检查：使用FOLLOW_RANGE属性
        double followDistance = observer.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (observer.distanceToSqr(target) > followDistance * followDistance) {
            return false;
        }

        // 视野角度检查
        Vec3 observerView = observer.getViewVector(1.0F);
        Vec3 targetDirection = target.getEyePosition().subtract(observer.getEyePosition()).normalize();
        double angle = Math.toDegrees(Math.acos(observerView.dot(targetDirection)));
        if (angle >= Config.VIEW_FIELD_ANGLE.get() / 2.0D) {
            return false;
        }

        // 光照等级检查（夜视豁免）
        if (!hasNightVision(observer)) {
            int lightLevel = target.level().getRawBrightness(target.blockPosition(), 0);
            if (lightLevel >= Config.MIN_STEALTH_LIGHT_LEVEL.get() && lightLevel <= Config.MAX_STEALTH_LIGHT_LEVEL.get()) {
                return false;
            }
        }

        // 视线检查（包含方块和实体遮挡）
        return LineOfSightChecker.hasLineOfSight(observer, target);
    }

    /**
     * 获取观察者能看到的所有目标
     */
    public static List<LivingEntity> getVisibleTargets(LivingEntity observer) {
        // 使用FOLLOW_RANGE属性确定扫描范围
        double followDistance = observer.getAttributeValue(Attributes.FOLLOW_RANGE);

        return observer.level().getEntitiesOfClass(
            LivingEntity.class,
            observer.getBoundingBox().inflate(followDistance),
            target -> canSee(observer, target)
        );
    }

    /**
     * 检查实体是否拥有夜视能力
     */
    public static boolean hasNightVision(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.NIGHT_VISION)) return true;

        EntityType<?> type = entity.getType();
        for (String entry : Config.NIGHT_VISION_ENTITIES.get()) {
            if (entry.startsWith("#")) {
                // 标签检查
                TagKey<EntityType<?>> tag = TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.parse(entry.substring(1))
                );
                if (type.is(tag)) return true;
            } else {
                // 直接ID检查
                ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(type);
                if (Objects.equals(entityId, ResourceLocation.parse(entry))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查实体是否失明
     */
    public static boolean isBlind(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null && Config.BLIND_ENTITIES.get().contains(entityId.toString());
    }

    /**
     * 检查实体是否失聪
     */
    public static boolean isDeaf(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null && Config.DEAF_ENTITIES.get().contains(entityId.toString());
    }
}
