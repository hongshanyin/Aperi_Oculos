package io.github.Sorcery_Dynasties.aperioculos.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * 振动感知事件
 * 当实体感知到GameEvent并被吸引时触发
 *
 * AI模组应监听此事件，并让怪物前往 sourcePos 位置调查
 * AI模组应根据gameEvent类型自行决定调查持续时间
 */
public class VibrationPerceivedEvent extends Event {
    private final LivingEntity listener;
    private final Vec3 sourcePos;
    private final GameEvent gameEvent;
    private final double effectiveRange;
    private final double actualDistance;
    @Nullable
    private final Entity sourceEntity;

    public VibrationPerceivedEvent(LivingEntity listener, Vec3 sourcePos, GameEvent gameEvent,
                                   double effectiveRange, double actualDistance,
                                   @Nullable Entity sourceEntity) {
        this.listener = listener;
        this.sourcePos = sourcePos;
        this.gameEvent = gameEvent;
        this.effectiveRange = effectiveRange;
        this.actualDistance = actualDistance;
        this.sourceEntity = sourceEntity;
    }

    public LivingEntity getListener() { return listener; }

    /**
     * 获取声音源位置（怪物应该前往调查的位置）
     */
    public Vec3 getSourcePos() { return sourcePos; }

    public GameEvent getGameEvent() { return gameEvent; }
    public double getEffectiveRange() { return effectiveRange; }
    public double getActualDistance() { return actualDistance; }

    @Nullable
    public Entity getSourceEntity() { return sourceEntity; }
}