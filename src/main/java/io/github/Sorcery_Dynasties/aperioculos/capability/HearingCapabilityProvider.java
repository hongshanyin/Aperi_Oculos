package io.github.Sorcery_Dynasties.aperioculos.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HearingCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<IHearingCapability> HEARING_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    private final IHearingCapability instance = new HearingCapability();
    private final LazyOptional<IHearingCapability> holder = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == HEARING_CAPABILITY ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("hearingMultiplier", instance.getHearingMultiplier());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("hearingMultiplier")) {
            instance.setHearingMultiplier(tag.getDouble("hearingMultiplier"));
        }
    }

    public void invalidate() {
        holder.invalidate();
    }
}