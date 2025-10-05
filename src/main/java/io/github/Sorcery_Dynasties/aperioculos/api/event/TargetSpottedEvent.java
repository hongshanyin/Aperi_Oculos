package io.github.Sorcery_Dynasties.aperioculos.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the FORGE event bus when an entity 'sees' a target according to Aperi Oculos's rules.
 * This event is server-side only.
 */
public class TargetSpottedEvent extends Event {
    private final LivingEntity observer;
    private final LivingEntity target;

    public TargetSpottedEvent(LivingEntity observer, LivingEntity target) {
        this.observer = observer;
        this.target = target;
    }

    public LivingEntity getObserver() {
        return observer;
    }

    public LivingEntity getTarget() {
        return target;
    }
}