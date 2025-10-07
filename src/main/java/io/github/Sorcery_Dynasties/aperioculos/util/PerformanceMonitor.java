package io.github.Sorcery_Dynasties.aperioculos.util;

import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 性能监控器
 * 定期输出缓存统计和性能指标
 */
@Mod.EventBusSubscriber
public class PerformanceMonitor {
    private static int tickCounter = 0;
    private static final int REPORT_INTERVAL_TICKS = 6000; // 每5分钟（6000 ticks）

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!Config.LOG_PERFORMANCE_METRICS.get()) return;

        tickCounter++;
        if (tickCounter >= REPORT_INTERVAL_TICKS) {
            tickCounter = 0;
            reportCacheStatistics();
        }
    }

    private static void reportCacheStatistics() {
        var losCache = PerceptionCache.LINE_OF_SIGHT_CACHE;
        var stats = losCache.stats();

        PerceptionLogger.logCacheStats(
                "LineOfSight",
                stats.hitCount(),
                stats.missCount(),
                losCache.size()
        );
    }
}