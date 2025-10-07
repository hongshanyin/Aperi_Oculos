package io.github.Sorcery_Dynasties.aperioculos.util;

import com.mojang.logging.LogUtils;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 感知系统日志工具类
 * 提供条件性日志记录，可通过配置文件控制
 */
public class PerceptionLogger {
    private static final Logger LOGGER = LogUtils.getLogger();

    // 采样计数器（线程安全）
    private static final AtomicLong visionEventCounter = new AtomicLong(0);
    private static final AtomicLong vibrationEventCounter = new AtomicLong(0);

    // === 视觉事件日志 ===

    /**
     * 记录目标被发现事件
     */
    public static void logTargetSpotted(LivingEntity observer, LivingEntity target,
                                        double distance, double fovAngle) {
        if (!shouldLogVisionEvent()) return;

        LOGGER.info("[VISION] {} spotted {} at distance {:.2f}m (FoV angle: {:.1f}°)",
                getEntityName(observer),
                getEntityName(target),
                distance,
                fovAngle
        );
    }

    /**
     * 记录视觉检查失败原因
     */
    public static void logVisionCheckFailed(LivingEntity observer, LivingEntity target, String reason) {
        if (!shouldLogVisionEvent()) return;

        LOGGER.debug("[VISION] {} failed to see {} - Reason: {}",
                getEntityName(observer),
                getEntityName(target),
                reason
        );
    }

    // === 振动事件日志 ===

    /**
     * 记录振动被感知事件
     */
    public static void logVibrationPerceived(LivingEntity listener, GameEvent gameEvent,
                                             Vec3 sourcePos, double distance, double effectiveRange) {
        if (!shouldLogVibrationEvent()) return;

        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);

        LOGGER.info("[VIBRATION] {} perceived {} at ({:.1f}, {:.1f}, {:.1f}) - Distance: {:.2f}m / Range: {:.2f}m",
                getEntityName(listener),
                eventId != null ? eventId.toString() : "unknown_event",
                sourcePos.x, sourcePos.y, sourcePos.z,
                distance,
                effectiveRange
        );
    }

    /**
     * 记录振动检查失败原因
     */
    public static void logVibrationBlocked(LivingEntity listener, GameEvent gameEvent, String reason) {
        if (!shouldLogVibrationEvent()) return;

        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);

        LOGGER.debug("[VIBRATION] {} blocked from hearing {} - Reason: {}",
                getEntityName(listener),
                eventId != null ? eventId.toString() : "unknown_event",
                reason
        );
    }

    // === Capability 日志 ===

    /**
     * 记录Capability附加事件
     */
    public static void logCapabilityAttached(Entity entity) {
        if (!shouldLogCapability()) return;

        LOGGER.debug("[CAPABILITY] Attached hearing capability to {}",
                getEntityName(entity)
        );
    }

    /**
     * 记录听力乘数变化
     */
    public static void logHearingMultiplierChanged(Entity entity, double oldValue, double newValue) {
        if (!shouldLogCapability()) return;

        LOGGER.info("[CAPABILITY] {} hearing multiplier changed: {:.2f} -> {:.2f}",
                getEntityName(entity),
                oldValue,
                newValue
        );
    }

    // === 缓存日志 ===

    /**
     * 记录缓存命中
     */
    public static void logCacheHit(String cacheType, Object key) {
        if (!shouldLogCache()) return;

        LOGGER.debug("[CACHE] {} cache HIT for key: {}",
                cacheType,
                key
        );
    }

    /**
     * 记录缓存未命中
     */
    public static void logCacheMiss(String cacheType, Object key) {
        if (!shouldLogCache()) return;

        LOGGER.debug("[CACHE] {} cache MISS for key: {}",
                cacheType,
                key
        );
    }

    /**
     * 记录缓存统计
     */
    public static void logCacheStats(String cacheType, long hitCount, long missCount, long size) {
        if (!shouldLogCache()) return;

        double hitRate = hitCount + missCount > 0
                ? (double) hitCount / (hitCount + missCount) * 100
                : 0;

        LOGGER.info("[CACHE] {} stats - Hit rate: {:.1f}% ({}/{}) | Size: {}",
                cacheType,
                hitRate,
                hitCount,
                hitCount + missCount,
                size
        );
    }

    // === 性能日志 ===

    /**
     * 记录视觉扫描性能
     */
    public static void logVisionScanPerformance(int playerCount, int mobCount,
                                                int checksPerformed, long durationMs) {
        if (!shouldLogPerformance()) return;

        LOGGER.info("[PERFORMANCE] Vision scan - Players: {} | Mobs: {} | Checks: {} | Duration: {}ms",
                playerCount,
                mobCount,
                checksPerformed,
                durationMs
        );
    }

    /**
     * 记录视线检查性能
     */
    public static void logLineOfSightPerformance(int totalChecks, int cachedChecks, long avgDurationNs) {
        if (!shouldLogPerformance()) return;

        double cacheHitRate = totalChecks > 0
                ? (double) cachedChecks / totalChecks * 100
                : 0;

        LOGGER.info("[PERFORMANCE] LoS checks - Total: {} | Cached: {:.1f}% | Avg duration: {}μs",
                totalChecks,
                cacheHitRate,
                avgDurationNs / 1000
        );
    }

    // === 系统状态日志 ===

    /**
     * 记录系统初始化
     */
    public static void logSystemInitialized(String systemName) {
        if (!isDebugEnabled()) return;

        LOGGER.info("[SYSTEM] {} initialized and ready", systemName);
    }

    /**
     * 记录配置加载
     */
    public static void logConfigLoaded() {
        if (!isDebugEnabled()) return;

        LOGGER.info("[CONFIG] Aperi Oculos configuration loaded:");
        LOGGER.info("  - Vision scan rate: {} ticks", Config.VISION_SCAN_RATE_TICKS.get());
        LOGGER.info("  - LoS cache duration: {} ticks", Config.LINE_OF_SIGHT_CACHE_DURATION_TICKS.get());
        LOGGER.info("  - Default hearing multiplier: {}", Config.DEFAULT_HEARING_MULTIPLIER.get());
        LOGGER.info("  - Debug logging: {}", Config.ENABLE_DEBUG_LOGGING.get());
    }

    // === 错误日志 ===

    /**
     * 记录错误
     */
    public static void logError(String context, Throwable throwable) {
        LOGGER.error("[ERROR] {} - {}", context, throwable.getMessage(), throwable);
    }

    /**
     * 记录警告
     */
    public static void logWarning(String message, Object... args) {
        LOGGER.warn("[WARNING] " + message, args);
    }

    // === 辅助方法 ===

    /**
     * 检查是否应记录视觉事件
     */
    private static boolean shouldLogVisionEvent() {
        if (!isDebugEnabled() || !Config.LOG_VISION_EVENTS.get()) {
            return false;
        }
        return shouldSample(visionEventCounter);
    }

    /**
     * 检查是否应记录振动事件
     */
    private static boolean shouldLogVibrationEvent() {
        if (!isDebugEnabled() || !Config.LOG_VIBRATION_EVENTS.get()) {
            return false;
        }
        return shouldSample(vibrationEventCounter);
    }

    /**
     * 检查是否应记录Capability操作
     */
    private static boolean shouldLogCapability() {
        return isDebugEnabled() && Config.LOG_CAPABILITY_OPERATIONS.get();
    }

    /**
     * 检查是否应记录缓存操作
     */
    private static boolean shouldLogCache() {
        return isDebugEnabled() && Config.LOG_CACHE_OPERATIONS.get();
    }

    /**
     * 检查是否应记录性能指标
     */
    private static boolean shouldLogPerformance() {
        return isDebugEnabled() && Config.LOG_PERFORMANCE_METRICS.get();
    }

    /**
     * 检查调试日志总开关
     */
    private static boolean isDebugEnabled() {
        return Config.ENABLE_DEBUG_LOGGING.get();
    }

    /**
     * 采样判断
     */
    private static boolean shouldSample(AtomicLong counter) {
        int samplingRate = Config.LOG_SAMPLING_RATE.get();
        if (samplingRate <= 1) {
            return true;
        }
        return counter.incrementAndGet() % samplingRate == 0;
    }

    /**
     * 获取实体显示名称
     */
    private static String getEntityName(Entity entity) {
        if (entity == null) {
            return "null";
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String name = entityId != null ? entityId.toString() : entity.getType().toString();

        // 如果实体有自定义名称，附加显示
        if (entity.hasCustomName()) {
            return name + "[" + entity.getCustomName().getString() + "]";
        }

        return name + "@" + Integer.toHexString(entity.getId());
    }
}