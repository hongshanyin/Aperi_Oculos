package io.github.Sorcery_Dynasties.aperioculos.attributes;

import com.google.common.collect.ImmutableMap;
import io.github.Sorcery_Dynasties.aperioculos.AperiOculos;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.stream.Stream;

@EventBusSubscriber(modid = AperiOculos.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AperiOculos.MOD_ID);

    // [修正] 属性改为乘数，更符合新的听觉模型
    public static final RegistryObject<Attribute> HEARING_MULTIPLIER = ATTRIBUTES.register("hearing_multiplier",
            () -> new RangedAttribute("attribute.name.aperioculos.hearing_multiplier", Config.DEFAULT_HEARING_MULTIPLIER.get(), 0.0, 10.0).setSyncable(true)
    );

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // [修正] 这是为所有Mob实体添加属性的正确现代方法
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
            if (type.getBaseClass().isAssignableFrom(Mob.class)) {
                event.put((EntityType<? extends Mob>) type, Mob.createMobAttributes().add(HEARING_MULTIPLIER.get()).build());
            }
        }
    }
}