package io.github.Sorcery_Dynasties.aperioculos.capability;

import io.github.Sorcery_Dynasties.aperioculos.config.Config;

public class HearingCapability implements IHearingCapability {
    private double hearingMultiplier;

    public HearingCapability() {
        this.hearingMultiplier = Config.DEFAULT_HEARING_MULTIPLIER.get();
    }

    @Override
    public double getHearingMultiplier() {
        return hearingMultiplier;
    }

    @Override
    public void setHearingMultiplier(double multiplier) {
        this.hearingMultiplier = Math.max(0.0, Math.min(10.0, multiplier));
    }

    @Override
    public void reset() {
        this.hearingMultiplier = Config.DEFAULT_HEARING_MULTIPLIER.get();
    }
}