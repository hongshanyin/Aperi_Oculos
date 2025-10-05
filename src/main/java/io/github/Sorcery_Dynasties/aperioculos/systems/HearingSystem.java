package io.github.Sorcery_Dynasties.aperioculos.systems;

import io.github.Sorcery_Dynasties.aperioculos.api.AperiOculosAPI;
import io.github.Sorcery_Dynasties.aperioculos.api.event.SoundHeardEvent;
import io.github.Sorcery_Dynasties.aperioculos.attributes.ModAttributes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.PlaySoundEvent; // [修正] 正确的事件导入
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HearingSystem {

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) { // [修正] 正确的事件类
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        SoundEvent sound = event.getSound();
        if (sound == null) return;

        Vec3 sourcePos = event.getPos();
        float initialVolume = event.getVolume();
        // [修正] 服务器端无法获取衰减距离，使用基于音量的标准估算
        float maxRange = initialVolume * 16.0f;
        if (maxRange <= 0) return;

        // [修正] 正确的AABB构造
        AABB scanBounds = new AABB(sourcePos, sourcePos).inflate(maxRange);

        for (LivingEntity listener : level.getEntitiesOfClass(LivingEntity.class, scanBounds)) {
            if (AperiOculosAPI.isDeaf(listener) || listener instanceof Player) continue;

            AttributeInstance thresholdAttr = listener.getAttribute(ModAttributes.HEARING_THRESHOLD.get());
            if (thresholdAttr == null) continue;
            double hearingThreshold = thresholdAttr.getValue();

            float perceivedVolume = getPerceivedVolumeAt(listener, sourcePos, maxRange, initialVolume);

            if (perceivedVolume >= hearingThreshold) {
                MinecraftForge.EVENT_BUS.post(new SoundHeardEvent(listener, sourcePos, sound, perceivedVolume, hearingThreshold));
            }
        }
    }

    private float getPerceivedVolumeAt(LivingEntity listener, Vec3 sourcePos, float maxRange, float initialVolume) {
        double distance = listener.position().distanceTo(sourcePos);

        if (distance > maxRange) {
            return 0.0f;
        }

        float volumeAfterDistance = initialVolume * (1.0f - (float) (distance / maxRange));

        ClipContext context = new ClipContext(listener.getEyePosition(), sourcePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, listener);
        if (listener.level().clip(context).getType() != HitResult.Type.MISS) {
            return volumeAfterDistance * 0.1f; // Muffled sound penalty
        }

        return volumeAfterDistance;
    }
}