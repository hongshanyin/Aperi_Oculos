package io.github.Sorcery_Dynasties.aperioculos.systems;

import io.github.Sorcery_Dynasties.aperioculos.api.AperiOculosAPI;
import io.github.Sorcery_Dynasties.aperioculos.api.event.VibrationPerceivedEvent;
import io.github.Sorcery_Dynasties.aperioculos.capability.HearingCapabilityProvider;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import io.github.Sorcery_Dynasties.aperioculos.util.PerceptionLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 振动感知系统
 * 监听GameEvent（如投掷物落地、箭矢击中等），使怪物被吸引到声音源位置
 */
public class VibrationSystem {

    @SubscribeEvent
    public void onGameEvent(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        GameEvent gameEvent = event.getVanillaEvent();
        Vec3 sourcePos = event.getEventPosition();
        @Nullable Entity sourceEntity = event.getCause();

        // 检查是否在监听列表中
        if (!isMonitoredEvent(gameEvent)) {
            return;
        }

        int baseRadius = gameEvent.getNotificationRadius();
        if (baseRadius <= 0) {
            return;
        }

        // 使用配置的自定义吸引范围（如果设置了）
        double configuredRange = getConfiguredAttractionRange(gameEvent);
        double finalRange = configuredRange > 0 ? configuredRange : baseRadius;

        // 扩大扫描范围（根据最大可能的听力乘数）
        double maxMultiplier = Config.MAX_HEARING_MULTIPLIER.get();
        AABB scanBounds = new AABB(sourcePos, sourcePos).inflate(finalRange * maxMultiplier);

        for (Mob listener : serverLevel.getEntitiesOfClass(Mob.class, scanBounds)) {
            // 失聪检查
            if (AperiOculosAPI.isDeaf(listener)) {
                PerceptionLogger.logVibrationBlocked(listener, gameEvent, "Entity is deaf", sourcePos);
                continue;
            }

            // 排除事件源自身
            if (listener.equals(sourceEntity)) {
                continue;
            }

            // 获取听力乘数
            double hearingMultiplier = getHearingMultiplier(listener);
            double effectiveRange = finalRange * hearingMultiplier;

            // 距离检查
            double distance = listener.position().distanceTo(sourcePos);
            boolean isOccluded = false;

            if (distance > effectiveRange) {

                // 这个日志现在只记录最常见的“距离太远”情况
                // LOGGING

                PerceptionLogger.logVibrationBlocked(listener, gameEvent, sourcePos, distance, effectiveRange, false);
                continue;
            }

            // 遮挡检查：如果开启，则将有效范围减半
            if (Config.ENABLE_VIBRATION_OCCLUSION.get() && isVibrationBlocked(listener, sourcePos)) {
                double occludedRange = effectiveRange * 0.5;
                // 应用惩罚后再次检查距离
                if (distance > occludedRange) {
                    // 这个日志专门记录因遮挡惩罚而失败的情况
                    // // LOGGING
                    PerceptionLogger.logVibrationBlocked(listener, gameEvent, sourcePos, distance, occludedRange, true);
                    continue;
                }
            }

            // 如果通过了所有检查，则判定为成功感知
            // // LOGGING
            PerceptionLogger.logVibrationPerceived(listener, gameEvent, sourcePos, distance, effectiveRange, sourceEntity);

            // 广播事件，包含持续时间信息
            int attractionDuration = Config.VIBRATION_ATTRACTION_DURATION_TICKS.get();
            MinecraftForge.EVENT_BUS.post(new VibrationPerceivedEvent(
                    listener, sourcePos, gameEvent, effectiveRange, distance, sourceEntity, attractionDuration
            ));
        }
    }

    /**
     * 获取实体的听力乘数
     */
    private double getHearingMultiplier(LivingEntity entity) {
        return entity.getCapability(HearingCapabilityProvider.HEARING_CAPABILITY)
                .map(cap -> cap.getHearingMultiplier())
                .orElse(Config.DEFAULT_HEARING_MULTIPLIER.get());
    }

    /**
     * 检查振动是否被遮挡
     */
    private boolean isVibrationBlocked(LivingEntity listener, Vec3 sourcePos) {
        Vec3 listenerPos = listener.position().add(0, listener.getEyeHeight() * 0.5, 0);

        ClipContext context = new ClipContext(
                listenerPos, sourcePos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                listener
        );

        return listener.level().clip(context).getType() != HitResult.Type.MISS;
    }

    /**
     * 检查GameEvent是否在监听列表中
     */
    private boolean isMonitoredEvent(GameEvent event) {
        List<? extends String> monitoredEvents = Config.MONITORED_GAME_EVENTS.get();

        // 空列表表示监听所有事件
        if (monitoredEvents.isEmpty()) {
            return true;
        }

        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(event);
        if (eventId == null) {
            return false;
        }

        return monitoredEvents.contains(eventId.toString());
    }

    /**
     * 获取特定事件的配置吸引范围
     */
    private double getConfiguredAttractionRange(GameEvent event) {
        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(event);
        if (eventId == null) {
            return 0;
        }

        return Config.getCustomAttractionRanges()
                .getOrDefault(eventId.toString(), 0.0);
    }
}