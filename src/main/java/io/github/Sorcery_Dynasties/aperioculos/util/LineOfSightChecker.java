package io.github.Sorcery_Dynasties.aperioculos.util;

import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffects;

public class LineOfSightChecker {

    /**
     * 检查两个实体之间是否有视线
     * @param observer 观察者
     * @param target 目标
     * @return true表示视线畅通
     */

    public static boolean hasLineOfSight(LivingEntity observer, LivingEntity target) {
        PerceptionCache.LineOfSightKey cacheKey =
                PerceptionCache.LineOfSightKey.create(observer, target);

        Boolean cached = PerceptionCache.LINE_OF_SIGHT_CACHE.getIfPresent(cacheKey);
        if (cached != null) {
            PerceptionLogger.logCacheHit("LineOfSight", cacheKey);
            return cached;
        }

        PerceptionLogger.logCacheMiss("LineOfSight", cacheKey);

        boolean result = performLineOfSightCheck(observer, target);
        PerceptionCache.LINE_OF_SIGHT_CACHE.put(cacheKey, result);

        return result;
    }

    private static boolean performLineOfSightCheck(LivingEntity observer, LivingEntity target) {
        // 新增：发光效果检查，无视障碍物
        if (target.hasEffect(MobEffects.GLOWING)) {
            return true;
        }

        Vec3 observerEye = observer.getEyePosition();
        Vec3 targetEye = target.getEyePosition();

        // 方块遮挡检测
        ClipContext context = new ClipContext(
                observerEye,
                targetEye,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                observer
        );

        HitResult hitResult = observer.level().clip(context);

        // 如果光线追踪没有击中方块，视线畅通
        if (hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }

        // 实体遮挡检测（可选）
        if (Config.ENTITY_OCCLUSION_ENABLED.get()) {
            return !isBlockedByEntity(observer, target, observerEye, targetEye);
        }

        return false;
    }

    /**
     * 检查是否被其他实体遮挡
     */
    private static boolean isBlockedByEntity(LivingEntity observer, LivingEntity target,
                                             Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        double minVolume = Config.MIN_BLOCKING_VOLUME.get();

        // 获取路径上的所有实体
        return observer.level().getEntitiesOfClass(
                LivingEntity.class,
                observer.getBoundingBox().minmax(target.getBoundingBox()),
                entity -> {
                    // 排除观察者和目标自身
                    if (entity.equals(observer) || entity.equals(target)) {
                        return false;
                    }

                    // 检查实体体积
                    double volume = entity.getBoundingBox().getSize();
                    if (volume < minVolume) {
                        return false;
                    }

                    // 玩家遮挡开关
                    if (entity.isSpectator() ||
                            (!Config.PLAYERS_BLOCK_VISION.get() && entity.getType().toString().contains("player"))) {
                        return false;
                    }

                    // 检查实体是否在视线路径上
                    return isOnPath(entity.position(), start, end);
                }
        ).size() > 0;
    }

    private static boolean isOnPath(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 toPoint = point.subtract(start);
        double projection = toPoint.dot(direction);

        if (projection < 0 || projection > start.distanceTo(end)) {
            return false;
        }

        Vec3 closestPoint = start.add(direction.scale(projection));
        return closestPoint.distanceTo(point) < 0.5; // 0.5格容差
    }
}
