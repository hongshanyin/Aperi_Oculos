package io.github.Sorcery_Dynasties.aperioculos;

import com.mojang.logging.LogUtils;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import io.github.Sorcery_Dynasties.aperioculos.systems.VibrationSystem;
import io.github.Sorcery_Dynasties.aperioculos.systems.VisionSystem;
import io.github.Sorcery_Dynasties.aperioculos.util.PerceptionLogger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AperiOculos.MOD_ID)
public class AperiOculos {
    public static final String MOD_ID = "aperioculos";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AperiOculos(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 注册配置文件
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC,
                "aperioculos-common.toml"
        );

        // 监听配置加载完成事件
        modEventBus.addListener(this::onCommonSetup);

        // 注册核心系统到Forge事件总线
        MinecraftForge.EVENT_BUS.register(new VisionSystem());
        MinecraftForge.EVENT_BUS.register(new VibrationSystem());

        LOGGER.info("Aperi Oculos: The eyes and ears of the world are now open.");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 记录配置加载信息
            PerceptionLogger.logConfigLoaded();

            // 记录系统初始化
            PerceptionLogger.logSystemInitialized("Vision System");
            PerceptionLogger.logSystemInitialized("Vibration System");
            PerceptionLogger.logSystemInitialized("Capability System");

            LOGGER.info("  - Vision System: Active (with entity occlusion support)");
            LOGGER.info("  - Vibration System: Active (GameEvent-based)");
            LOGGER.info("  - Capability System: Active (hearing multiplier storage)");

            if (Config.ENABLE_DEBUG_LOGGING.get()) {
                LOGGER.warn("⚠️ Debug logging is ENABLED - this may impact performance!");
            }
        });
    }
}