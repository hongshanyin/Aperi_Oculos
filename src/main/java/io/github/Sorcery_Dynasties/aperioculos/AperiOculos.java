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
        MinecraftForge.EVENT_BUS.register(new io.github.Sorcery_Dynasties.aperioculos.systems.VanillaAIIntegration());

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
            LOGGER.info("  - Vanilla AI Integration: Active");

            // 记录原版AI集成配置
            if (Config.OVERRIDE_VANILLA_HAS_LINE_OF_SIGHT.get()) {
                LOGGER.info("    → Vanilla hasLineOfSight() overridden with cached Aperi Oculos implementation");
            }
            if (Config.DISABLE_VANILLA_TARGET_GOALS.get()) {
                LOGGER.warn("    → Vanilla target selection goals will be removed from mobs");
            }

            if (Config.ENABLE_DEBUG_LOGGING.get()) {
                LOGGER.warn("⚠️ Debug logging is ENABLED - this may impact performance!");
            }

            // 检查配置完整性
            checkConfigIntegrity();
        });
    }

    /**
     * 检查配置文件是否包含所有新增配置项
     */
    private void checkConfigIntegrity() {
        // 检查customAttractionRanges是否包含step配置
        boolean hasStepConfig = Config.CUSTOM_ATTRACTION_RANGES.get().stream()
                .anyMatch(entry -> entry.toLowerCase().contains("minecraft:step"));

        if (!hasStepConfig) {
            LOGGER.warn("=".repeat(60));
            LOGGER.warn("⚠️ OUTDATED CONFIG DETECTED!");
            LOGGER.warn("Your aperioculos-common.toml is missing new sound range configurations.");
            LOGGER.warn("Recommended actions:");
            LOGGER.warn("  1. Delete 'config/aperioculos-common.toml'");
            LOGGER.warn("  2. Restart the game to regenerate with new defaults");
            LOGGER.warn("Or manually add to [hearing] section:");
            LOGGER.warn("  customAttractionRanges = [");
            LOGGER.warn("    \"minecraft:step = 8.0\",");
            LOGGER.warn("    \"minecraft:swim = 6.0\",");
            LOGGER.warn("    ... (see Config.java for full list)");
            LOGGER.warn("  ]");
            LOGGER.warn("=".repeat(60));
        }

        // 检查silentArmorMaterials是否存在
        if (Config.SILENT_ARMOR_MATERIALS.get().isEmpty()) {
            LOGGER.warn("⚠️ Silent armor materials list is empty!");
            LOGGER.warn("Crouching will reduce sound range for all entities.");
            LOGGER.warn("Consider adding: silentArmorMaterials = [\"leather\", \"wool\", \"cloth\"]");
        }
    }
}