package io.github.Sorcery_Dynasties.aperioculos.systems;

import io.github.Sorcery_Dynasties.aperioculos.api.AperiOculosAPI;
import io.github.Sorcery_Dynasties.aperioculos.api.event.VibrationPerceivedEvent;
import io.github.Sorcery_Dynasties.aperioculos.capability.HearingCapabilityProvider;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
// 新增 import
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

public class HearingSystem {

    @SubscribeEvent
    public void onGameEvent(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // event.getVanillaEvent() 正确返回 GameEvent
        GameEvent gameEvent = event.getVanillaEvent();
        Vec3 sourcePos = event.getEventPosition();
        @Nullable Entity sourceEntity = event.getCause();

        if (!isMonitoredEvent(gameEvent)) {
            return;
        }

        // 使用正确的 getter 方法
        int baseRadius = gameEvent.getNotificationRadius();
        if (baseRadius <= 0) {
            return;
        }

        AABB scanBounds = new AABB(sourcePos, sourcePos).inflate(baseRadius * 3.0);

        for (Mob listener : serverLevel.getEntitiesOfClass(Mob.class, scanBounds)) {
            if (AperiOculosAPI.isDeaf(listener)) continue;
            if (listener.equals(sourceEntity)) continue;

            double hearingMultiplier = getHearingMultiplier(listener);
            double effectiveRange = baseRadius * hearingMultiplier;

            double distance = listener.position().distanceTo(sourcePos);
            if (distance > effectiveRange) {
                continue;
            }

            if (isVibrationBlocked(listener, sourcePos)) {
                continue;
            }

            // 现在传入 gameEvent 对象，类型匹配正确
            MinecraftForge.EVENT_BUS.post(new VibrationPerceivedEvent(
                    listener, sourcePos, gameEvent, effectiveRange, distance, sourceEntity
            ));
        }
    }

    private double getHearingMultiplier(LivingEntity entity) {
        return entity.getCapability(HearingCapabilityProvider.HEARING_CAPABILITY)
                .map(cap -> cap.getHearingMultiplier())
                .orElse(Config.DEFAULT_HEARING_MULTIPLIER.get());
    }

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
     * 检查GameEvent是否在监听列表中 (已修正)
     */
    private boolean isMonitoredEvent(GameEvent event) {
        List<? extends String> monitoredEvents = Config.MONITORED_GAME_EVENTS.get();
        if (monitoredEvents.isEmpty()) {
            return true;
        }

        // 关键修正：通过查询注册表来获取 GameEvent 的 ResourceLocation
        ResourceLocation eventId = BuiltInRegistries.GAME_EVENT.getKey(event);
        if (eventId == null) {
            return false; // 如果事件未注册，则不监听
        }

        return monitoredEvents.contains(eventId.toString());
    }
}