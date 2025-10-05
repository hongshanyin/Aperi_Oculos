package io.github.Sorcery_Dynasties.aperioculos.api.event;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the FORGE event bus when an entity 'hears' a sound.
 * This event is server-side only.
 */
public class SoundHeardEvent extends Event {
    private final LivingEntity listener;
    private final Vec3 sourcePos;
    private final SoundEvent sound;
    private final float perceivedVolume; // The final volume calculated at the listener's position
    private final double hearingThreshold; // The listener's hearing threshold attribute value

    public SoundHeardEvent(LivingEntity listener, Vec3 sourcePos, SoundEvent sound, float perceivedVolume, double hearingThreshold) {
        this.listener = listener;
        this.sourcePos = sourcePos;
        this.sound = sound;
        this.perceivedVolume = perceivedVolume;
        this.hearingThreshold = hearingThreshold;
    }

    public LivingEntity getListener() { return listener; }
    public Vec3 getSourcePos() { return sourcePos; }
    public SoundEvent getSound() { return sound; }
    public float getPerceivedVolume() { return perceivedVolume; }
    public double getHearingThreshold() { return hearingThreshold; }
}