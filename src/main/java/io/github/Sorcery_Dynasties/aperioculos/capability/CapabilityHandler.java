package io.github.Sorcery_Dynasties.aperioculos.capability;

import io.github.Sorcery_Dynasties.aperioculos.AperiOculos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AperiOculos.MOD_ID)
public class CapabilityHandler {

    private static final ResourceLocation HEARING_CAP_ID =
            new ResourceLocation(AperiOculos.MOD_ID, "hearing");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            HearingCapabilityProvider provider = new HearingCapabilityProvider();
            event.addCapability(HEARING_CAP_ID, provider);
        }
    }
}