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
import org.jetbrains.annotations.Nullable;

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
     * @param observer 观察者实体（原始数据）
     * @param target 目标实体（原始数据）
     * @param distance 原始距离值
     * @param fovAngle 原始角度值
     * @param visionRange 原始视野范围值
     */
    public static void logTargetSpotted(LivingEntity observer, LivingEntity target,
                                        double distance, double fovAngle, double visionRange) {
        if (!shouldLogVisionEvent()) return; // 关键的性能检查点

        // --- 所有昂贵的操作（包括格式化）现在被安全地包裹在检查之后 ---
        LOGGER.info("[VISION] {} at {} spotted {} at {} - Distance: {:.2f}m / Range: {:.1f}m (FoV: {:.1f}°)",
                getEntityName(observer),
                formatPosition(observer),
                getEntityName(target),
                formatPosition(target),
                distance,       // 原始数据，由SLF4J格式化
                visionRange,    // 原始数据，由SLF4J格式化
                fovAngle        // 原始数据，由SLF4J格式化
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
     * @param listener 监听者实体（原始数据）
     * @param gameEvent 游戏事件（原始数据）
     * @param sourcePos 声源位置（原始数据）
     * @param distance 原始距离值
     * @param effectiveRange 原始有效范围值
     * @param sourceEntity 声源实体（原始数据，可为null）
     */
    public static void logVibrationPerceived(LivingEntity listener, GameEvent gameEvent,
                                             Vec3 sourcePos, double distance, double effectiveRange,
                                             @Nullable Entity sourceEntity) {
        if (!shouldLogVibrationEvent()) return; // 关键的性能检查点

        // --- 所有昂贵的操作（包括格式化）现在被安全地包裹在检查之后 ---
        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);
        String sourceName = sourceEntity != null ? getEntityName(sourceEntity) : "world";

        LOGGER.info("[VIBRATION] {} at {} perceived {} from {} at {} - Distance: {:.2f}m / Range: {:.2f}m",
                getEntityName(listener),
                formatPosition(listener),
                eventId != null ? eventId.toString() : "unknown_event",
                sourceName,
                formatPosition(sourcePos),
                distance,         // 原始数据，由SLF4J格式化
                effectiveRange    // 原始数据，由SLF4J格式化
        );
    }
    /**
     * 记录振动检查失败原因
     */

    // 保留旧方法，用于记录其他原因的失败，例如“失聪”
    public static void logVibrationBlocked(LivingEntity listener, GameEvent gameEvent, String simpleReason, Vec3 sourcePos) {
        if (!shouldLogVibrationEvent()) return;

        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);

        LOGGER.debug("[VIBRATION] {} at {} blocked from hearing {} at {} - Reason: {}",
                getEntityName(listener),
                formatPosition(listener),
                eventId != null ? eventId.toString() : "unknown_event",
                formatPosition(sourcePos),
                simpleReason
        );
    }
    /**
     * 记录振动被阻挡事件（距离/遮挡原因）
     * @param listener 监听者实体（原始数据）
     * @param gameEvent 游戏事件（原始数据）
     * @param sourcePos 声源位置（原始数据）
     * @param distance 原始距离值
     * @param effectiveRange 原始有效范围值
     * @param isOccluded 是否遮挡
     */
    public static void logVibrationBlocked(LivingEntity listener, GameEvent gameEvent, Vec3 sourcePos,
        double distance, double effectiveRange, boolean isOccluded) {
        if (!shouldLogVibrationEvent()) return; // 关键的性能检查点

        // --- 所有昂贵的操作（包括格式化）现在被安全地包裹在检查之后 ---
        String reason = String.format("Too far (%.2fm > %.2fm)", distance, effectiveRange);
        if (isOccluded) {
            reason += " with 50% occlusion penalty";
        }

        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);

        LOGGER.debug("[VIBRATION] {} at {} blocked from hearing {} at {} - Reason: {}",
                getEntityName(listener),
                formatPosition(listener),
                eventId != null ? eventId.toString() : "unknown_event",
                formatPosition(sourcePos),
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
     * @param entity 实体（原始数据）
     * @param oldValue 旧值（原始数据）
     * @param newValue 新值（原始数据）
     */
    public static void logHearingMultiplierChanged(Entity entity, double oldValue, double newValue) {
        if (!shouldLogCapability()) return;

        LOGGER.info("[CAPABILITY] {} hearing multiplier changed: {:.2f} -> {:.2f}",
                getEntityName(entity),
                oldValue,    // 原始数据，由SLF4J格式化
                newValue     // 原始数据，由SLF4J格式化
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
     * @param cacheType 缓存类型名称
     * @param hitCount 命中次数（原始数据）
     * @param missCount 未命中次数（原始数据）
     * @param size 缓存大小（原始数据）
     */
    public static void logCacheStats(String cacheType, long hitCount, long missCount, long size) {
        if (!shouldLogCache()) return; // 关键的性能检查点

        // --- 所有计算现在被安全地包裹在检查之后 ---
        double hitRate = hitCount + missCount > 0
                ? (double) hitCount / (hitCount + missCount) * 100
                : 0;

        LOGGER.info("[CACHE] {} stats - Hit rate: {:.1f}% ({}/{}) | Size: {}",
                cacheType,
                hitRate,            // 内部计算结果，由SLF4J格式化
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
     * @param totalChecks 总检查次数（原始数据）
     * @param cachedChecks 缓存命中次数（原始数据）
     * @param avgDurationNs 平均耗时纳秒（原始数据）
     */
    public static void logLineOfSightPerformance(int totalChecks, int cachedChecks, long avgDurationNs) {
        if (!shouldLogPerformance()) return; // 关键的性能检查点

        // --- 所有计算现在被安全地包裹在检查之后 ---
        double cacheHitRate = totalChecks > 0
                ? (double) cachedChecks / totalChecks * 100
                : 0;

        LOGGER.info("[PERFORMANCE] LoS checks - Total: {} | Cached: {:.1f}% | Avg duration: {}μs",
                totalChecks,
                cacheHitRate,         // 内部计算结果，由SLF4J格式化
                avgDurationNs / 1000  // 内部单位转换，由SLF4J格式化
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
    /**
     * 格式化实体坐标
     */
    private static String formatPosition(Entity entity) {
        if (entity == null) return "(null)";
        return formatPosition(entity.position());
    }

    /**
     * 格式化Vec3坐标
     */
    private static String formatPosition(Vec3 pos) {
        if (pos == null) return "(null)";
        return String.format("(%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z);
    }
}