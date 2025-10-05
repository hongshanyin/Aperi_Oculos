package io.github.Sorcery_Dynasties.aperioculos;

import com.mojang.logging.LogUtils;
import io.github.Sorcery_Dynasties.aperioculos.attributes.ModAttributes;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import io.github.Sorcery_Dynasties.aperioculos.systems.HearingSystem;
import io.github.Sorcery_Dynasties.aperioculos.systems.VisionSystem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AperiOculos.MOD_ID)
public class AperiOculos {
    public static final String MOD_ID = "aperioculos";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AperiOculos(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 1. 注册自定义属性（仅声明，实际使用Capability）
        ModAttributes.register(modEventBus);

        // 2. 注册配置文件
        ModLoadingContext.get().registerConfig(
            ModConfig.Type.COMMON,
            Config.SPEC,
            "aperioculos-common.toml"
        );

        // 3. 将核心系统注册到Forge事件总线
        MinecraftForge.EVENT_BUS.register(new VisionSystem());
        MinecraftForge.EVENT_BUS.register(new HearingSystem());

        // 注意：CapabilityHandler已通过@Mod.EventBusSubscriber自动注册
        // 无需手动注册

        LOGGER.info("Aperi Oculos: The eyes and ears of the world are now open.");
        LOGGER.info("  - Vision System: Active (with entity occlusion support)");
        LOGGER.info("  - Hearing System: Active (GameEvent-based vibration perception)");
        LOGGER.info("  - Capability System: Active (hearing multiplier storage)");
    }
}
