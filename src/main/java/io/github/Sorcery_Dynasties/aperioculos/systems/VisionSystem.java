package io.github.Sorcery_Dynasties.aperioculos.systems;

import io.github.Sorcery_Dynasties.aperioculos.api.AperiOculosAPI;
import io.github.Sorcery_Dynasties.aperioculos.api.event.TargetSpottedEvent;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import io.github.Sorcery_Dynasties.aperioculos.util.PerceptionLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VisionSystem {
    private int tickCounter = 0;
    private static final double MAX_SCAN_RADIUS = 64.0;

    // 性能统计
    private int totalChecksThisScan = 0;
    private int successfulSpottings = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < Config.VISION_SCAN_RATE_TICKS.get()) {
            return;
        }
        tickCounter = 0;

        // 重置统计
        totalChecksThisScan = 0;
        successfulSpottings = 0;
        long scanStartTime = System.currentTimeMillis();

        int totalPlayers = 0;
        int totalMobs = 0;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            totalPlayers++;

            ServerLevel level = player.serverLevel();
            AABB scanBounds = player.getBoundingBox().inflate(MAX_SCAN_RADIUS);

            for (PathfinderMob mob : level.getEntitiesOfClass(PathfinderMob.class, scanBounds)) {
                totalMobs++;
                totalChecksThisScan++;

                if (AperiOculosAPI.canSee(mob, player)) {
                    successfulSpottings++;

                    // 记录发现事件
                    double distance = mob.distanceTo(player);
                    double fovAngle = calculateFovAngle(mob, player);
                    PerceptionLogger.logTargetSpotted(mob, player, distance, fovAngle);

                    MinecraftForge.EVENT_BUS.post(new TargetSpottedEvent(mob, player));
                }
            }
        }

        // 记录性能指标
        long scanDuration = System.currentTimeMillis() - scanStartTime;
        PerceptionLogger.logVisionScanPerformance(
                totalPlayers,
                totalMobs,
                totalChecksThisScan,
                scanDuration
        );
    }

    private double calculateFovAngle(PathfinderMob observer, ServerPlayer target) {
        var observerView = observer.getViewVector(1.0F);
        var targetDirection = target.getEyePosition()
                .subtract(observer.getEyePosition()).normalize();
        return Math.toDegrees(Math.acos(observerView.dot(targetDirection)));
    }
}