package io.github.Sorcery_Dynasties.aperioculos.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PerceptionCache {
    // 混合过期策略：时间 + 粗粒度位置
    public record LineOfSightKey(UUID observer, UUID target, BlockPos observerChunk, BlockPos targetChunk) {
        public static LineOfSightKey create(LivingEntity observer, LivingEntity target) {
            // 使用2x2格子的粗粒度位置作为缓存键的一部分
            BlockPos observerPos = new BlockPos(
                (int)(observer.getX() / 2) * 2,
                (int)(observer.getY() / 2) * 2,
                (int)(observer.getZ() / 2) * 2
            );
            BlockPos targetPos = new BlockPos(
                (int)(target.getX() / 2) * 2,
                (int)(target.getY() / 2) * 2,
                (int)(target.getZ() / 2) * 2
            );
            return new LineOfSightKey(observer.getUUID(), target.getUUID(), observerPos, targetPos);
        }
    }

    // 视线缓存：包含位置信息的混合键 + 时间过期
    public static final Cache<LineOfSightKey, Boolean> LINE_OF_SIGHT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Config.LINE_OF_SIGHT_CACHE_DURATION_TICKS.get() * 50L, TimeUnit.MILLISECONDS)
            .maximumSize(1000) // 限制缓存大小
            .build();

    // 手动失效检查：当实体移动距离超过阈值时
    public static void invalidateIfMoved(LivingEntity entity, BlockPos lastPos) {
        if (lastPos != null && entity.blockPosition().distSqr(lastPos) > 4) { // 2格阈值
            // 清除所有与该实体相关的缓存（需要遍历，性能开销较大，谨慎使用）
            LINE_OF_SIGHT_CACHE.asMap().keySet().removeIf(key ->
                key.observer.equals(entity.getUUID()) || key.target.equals(entity.getUUID())
            );
        }
    }
}