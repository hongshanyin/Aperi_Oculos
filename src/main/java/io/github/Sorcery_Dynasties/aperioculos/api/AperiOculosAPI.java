package io.github.Sorcery_Dynasties.aperioculos.api;

import com.google.common.cache.Cache;
import com.mojang.datafixers.util.Pair;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import io.github.Sorcery_Dynasties.aperioculos.util.PerceptionCache;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes; // [修正] 导入
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AperiOculosAPI {

    public static boolean canSee(LivingEntity observer, LivingEntity target) {
        if (observer == null || target == null || !observer.isAlive() || !target.isAlive() || observer.equals(target)) {
            return false;
        }
        if (isBlind(observer)) return false;

        // [修正] getFollowDistance 只在 Mob 类中存在
        double followDistance = observer.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (observer.distanceToSqr(target) > followDistance * followDistance) {
            return false;
        }

        Vec3 observerView = observer.getViewVector(1.0F);
        // [修正] 目标方向应从观察者的眼睛位置开始计算，以获得更准确的角度
        Vec3 targetDirection = target.getEyePosition().subtract(observer.getEyePosition()).normalize();
        double angle = Math.toDegrees(Math.acos(observerView.dot(targetDirection)));
        if (angle >= Config.VIEW_FIELD_ANGLE.get() / 2.0D) {
            return false;
        }

        if (!hasNightVision(observer)) {
            int lightLevel = target.level().getRawBrightness(target.blockPosition(), 0);
            if (lightLevel >= Config.MIN_STEALTH_LIGHT_LEVEL.get() && lightLevel <= Config.MAX_STEALTH_LIGHT_LEVEL.get()) {
                return false;
            }
        }

        // [修正] 明确类型
        Pair<UUID, UUID> cacheKey = Pair.of(observer.getUUID(), target.getUUID());
        Boolean cachedResult = PerceptionCache.LINE_OF_SIGHT_CACHE.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        ClipContext context = new ClipContext(observer.getEyePosition(), target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, observer);
        boolean canSee = observer.level().clip(context).getType() == HitResult.Type.MISS;
        // [修正] 自动装箱 boolean -> Boolean
        PerceptionCache.LINE_OF_SIGHT_CACHE.put(cacheKey, canSee);
        return canSee;
    }

    public static List<LivingEntity> getVisibleTargets(LivingEntity observer) {
        // [修正] getFollowDistance 只在 Mob 类中存在
        double followDistance = (observer instanceof Mob mob) ? mob.getFollowDistance() : 16.0;
        return observer.level().getEntitiesOfClass(LivingEntity.class, observer.getBoundingBox().inflate(followDistance),
                e -> canSee(observer, e));
    }

    public static boolean hasNightVision(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.NIGHT_VISION)) return true;
        EntityType<?> type = entity.getType();
        for (String entry : Config.NIGHT_VISION_ENTITIES.get()) {
            if (entry.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(entry.substring(1)));
                if (type.is(tag)) return true;
            } else {
                // [修正] 正确的注册表访问
                if (Objects.equals(ForgeRegistries.ENTITY_TYPES.getKey(type), ResourceLocation.parse(entry))) return true;
            }
        }
        return false;
    }

    public static boolean isBlind(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null && Config.BLIND_ENTITIES.get().contains(entityId.toString());
    }

    public static boolean isDeaf(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null && Config.DEAF_ENTITIES.get().contains(entityId.toString());
    }
}