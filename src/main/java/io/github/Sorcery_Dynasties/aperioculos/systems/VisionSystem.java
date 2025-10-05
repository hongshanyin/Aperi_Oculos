package io.github.Sorcery_Dynasties.aperioculos.systems;

import io.github.Sorcery_Dynasties.aperioculos.api.AperiOculosAPI;
import io.github.Sorcery_Dynasties.aperioculos.api.event.TargetSpottedEvent;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VisionSystem {
    private int tickCounter = 0;
    // [修正] 定义一个固定的最大扫描半径，避免调用不存在的方法
    private static final double MAX_SCAN_RADIUS = 64.0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < Config.VISION_SCAN_RATE_TICKS.get()) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            ServerLevel level = player.serverLevel();
            AABB scanBounds = player.getBoundingBox().inflate(MAX_SCAN_RADIUS);

            for (PathfinderMob mob : level.getEntitiesOfClass(PathfinderMob.class, scanBounds)) {
                // canSee 内部会使用 mob 自己的 getFollowDistance 进行精确距离检查
                if (AperiOculosAPI.canSee(mob, player)) {
                    MinecraftForge.EVENT_BUS.post(new TargetSpottedEvent(mob, player));
                }
            }
        }
    }
}