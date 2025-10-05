package io.github.Sorcery_Dynasties.aperioculos.attributes;

import io.github.Sorcery_Dynasties.aperioculos.AperiOculos;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 自定义属性注册
 * 注意：由于技术限制，我们无法为所有原版/其他Mod的实体添加这些属性。
 * 这些属性主要用于：
 * 1. 本Mod的自定义实体
 * 2. 通过数据包为特定实体配置默认值
 * 3. 实际的听力乘数通过Capability系统存储
 */
@EventBusSubscriber(modid = AperiOculos.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AperiOculos.MOD_ID);

    /**
     * 听力乘数属性（可选）
     * 实际使用时通过Capability系统获取，而非此属性
     * 此属性仅作为声明，供未来可能的数据包配置使用
     */
    public static final RegistryObject<Attribute> HEARING_MULTIPLIER = ATTRIBUTES.register("hearing_multiplier",
            () -> new RangedAttribute("attribute.name.aperioculos.hearing_multiplier", 1.0, 0.0, 10.0).setSyncable(true)
    );

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    // 移除破坏性的 onEntityAttributeCreation 方法
    // 如果需要为自定义实体添加属性，应在实体类的 createAttributes() 方法中添加
}
