package io.github.Sorcery_Dynasties.aperioculos.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on the FORGE event bus when an entity perceives a vibration (GameEvent).
 * This replaces the previous sound-based system with GameEvent-based vibration perception.
 * This event is server-side only.
 */
public class VibrationPerceivedEvent extends Event {
    private final LivingEntity listener;
    private final Vec3 sourcePos;
    private final GameEvent gameEvent;
    private final double effectiveRange; // 有效感知距离
    private final double actualDistance; // 实际距离
    @Nullable
    private final Entity sourceEntity; // 可能为null

    public VibrationPerceivedEvent(LivingEntity listener, Vec3 sourcePos, GameEvent gameEvent,
                                    double effectiveRange, double actualDistance, @Nullable Entity sourceEntity) {
        this.listener = listener;
        this.sourcePos = sourcePos;
        this.gameEvent = gameEvent;
        this.effectiveRange = effectiveRange;
        this.actualDistance = actualDistance;
        this.sourceEntity = sourceEntity;
    }

    public LivingEntity getListener() { return listener; }
    public Vec3 getSourcePos() { return sourcePos; }
    public GameEvent getGameEvent() { return gameEvent; }
    public double getEffectiveRange() { return effectiveRange; }
    public double getActualDistance() { return actualDistance; }
    @Nullable
    public Entity getSourceEntity() { return sourceEntity; }
}