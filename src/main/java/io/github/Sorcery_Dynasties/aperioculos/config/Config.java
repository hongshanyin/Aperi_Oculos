package io.github.Sorcery_Dynasties.aperioculos.config;

import io.github.Sorcery_Dynasties.aperioculos.AperiOculos;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // === Vision Settings ===
    public static final ForgeConfigSpec.DoubleValue VIEW_FIELD_ANGLE;
    public static final ForgeConfigSpec.IntValue MIN_STEALTH_LIGHT_LEVEL;
    public static final ForgeConfigSpec.IntValue MAX_STEALTH_LIGHT_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_VISION_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLIND_ENTITIES;

    // === Entity Occlusion Settings ===
    public static final ForgeConfigSpec.BooleanValue ENTITY_OCCLUSION_ENABLED;
    public static final ForgeConfigSpec.DoubleValue MIN_BLOCKING_VOLUME;
    public static final ForgeConfigSpec.BooleanValue PLAYERS_BLOCK_VISION;

    // === Hearing Settings ===
    public static final ForgeConfigSpec.DoubleValue DEFAULT_HEARING_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MAX_HEARING_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEAF_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MONITORED_GAME_EVENTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ATTRACTION_RANGES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SILENT_ARMOR_MATERIALS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_VIBRATION_OCCLUSION;

    // === Performance Settings ===
    public static final ForgeConfigSpec.IntValue VISION_SCAN_RATE_TICKS;
    public static final ForgeConfigSpec.IntValue LINE_OF_SIGHT_CACHE_DURATION_TICKS;

    // === Vanilla AI Integration Settings (新增) ===
    public static final ForgeConfigSpec.BooleanValue OVERRIDE_VANILLA_HAS_LINE_OF_SIGHT;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_TARGET_GOALS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VANILLA_GOALS_WHITELIST;

    // === Logging Settings (新增) ===
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING;
    public static final ForgeConfigSpec.BooleanValue LOG_VISION_EVENTS;
    public static final ForgeConfigSpec.BooleanValue LOG_VIBRATION_EVENTS;
    public static final ForgeConfigSpec.BooleanValue LOG_CAPABILITY_OPERATIONS;
    public static final ForgeConfigSpec.BooleanValue LOG_CACHE_OPERATIONS;
    public static final ForgeConfigSpec.BooleanValue LOG_PERFORMANCE_METRICS;
    public static final ForgeConfigSpec.IntValue LOG_SAMPLING_RATE; // 采样率：每N次事件记录1次

    // 在 Config 类中添加静态方法解析自定义范围
    public static Map<String, Double> parseCustomAttractionRanges() {
        Map<String, Double> ranges = new HashMap<>();

        for (String entry : CUSTOM_ATTRACTION_RANGES.get()) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    String eventId = parts[0].trim();
                    double range = Double.parseDouble(parts[1].trim());
                    ranges.put(eventId, range);
                } catch (NumberFormatException e) {
                    AperiOculos.LOGGER.warn("Invalid attraction range format: {}", entry);
                }
            }
        }

        return ranges;
    }

    // 缓存解析结果
    private static Map<String, Double> cachedRanges = null;

    public static Map<String, Double> getCustomAttractionRanges() {
        if (cachedRanges == null) {
            cachedRanges = parseCustomAttractionRanges();
        }
        return cachedRanges;
    }
    static {
        BUILDER.push("vision");
        BUILDER.comment("Settings related to how entities 'see' the world.");

        VIEW_FIELD_ANGLE = BUILDER
                .comment("Field of view angle in degrees (180 = half sphere, 360 = full sphere)")
                .defineInRange("viewFieldAngle", 180.0, 0.0, 360.0);

        MIN_STEALTH_LIGHT_LEVEL = BUILDER
                .comment("Minimum light level for stealth (targets in this range are harder to see)")
                .defineInRange("minStealthLightLevel", 0, 0, 15);

        MAX_STEALTH_LIGHT_LEVEL = BUILDER
                .comment("Maximum light level for stealth")
                .defineInRange("maxStealthLightLevel", 7, 0, 15);

        NIGHT_VISION_ENTITIES = BUILDER
                .comment(
                        "Entities immune to light-level checks.",
                        "Supports entity IDs (e.g., 'minecraft:creeper') and tags (e.g., '#minecraft:undead')."
                )
                .defineList("nightVisionEntities",
                        List.of("#minecraft:undead", "minecraft:spider", "minecraft:cave_spider", "minecraft:enderman"),
                        s -> true);

        BLIND_ENTITIES = BUILDER
                .comment("Entities that can never 'see' targets through this framework.")
                .defineList("blindEntities", List.of("minecraft:warden"), s -> true);

        BUILDER.pop();

        BUILDER.push("entityOcclusion");
        BUILDER.comment("Settings for entity-based line of sight blocking.");

        ENTITY_OCCLUSION_ENABLED = BUILDER
                .comment("Enable entity occlusion checks (entities can block line of sight)")
                .define("enabled", true);

        MIN_BLOCKING_VOLUME = BUILDER
                .comment("Minimum entity bounding box volume to block vision (filters out small entities)")
                .defineInRange("minBlockingVolume", 0.5, 0.0, 100.0);

        PLAYERS_BLOCK_VISION = BUILDER
                .comment("Whether players can block other entities' vision")
                .define("playersBlockVision", false);

        BUILDER.pop();

        // 在 [hearing] 部分添加新配置

        BUILDER.push("hearing");
        BUILDER.comment("Settings for vibration-based attraction (GameEvent system).");

        DEFAULT_HEARING_MULTIPLIER = BUILDER
                .comment(
                        "Default hearing multiplier for mobs (stored via Capability system).",
                        "This value multiplies the attraction range of sounds."
                )
                .defineInRange("defaultHearingMultiplier", 1.0, 0.0, 10.0);

// 新增：最大听力乘数（用于计算扫描范围）
        MAX_HEARING_MULTIPLIER = BUILDER
                .comment("Maximum possible hearing multiplier (affects scan optimization)")
                .defineInRange("maxHearingMultiplier", 2.0, 1.0, 10.0);

        DEAF_ENTITIES = BUILDER
                .comment("Entities that can never perceive vibrations through this framework.")
                .defineList("deafEntities", List.of(), s -> true);

        MONITORED_GAME_EVENTS = BUILDER
                .comment(
                        "List of GameEvent IDs to monitor for vibration perception.",
                        "Recommended events for projectile attraction:",
                        "  - minecraft:projectile_land (箭、三叉戟等落地)",
                        "  - minecraft:item_interact_finish (使用物品完成)",
                        "  - minecraft:hit_ground (实体落地)",
                        "Leave empty to monitor ALL GameEvents."
                )
                .defineList("monitoredGameEvents",
                        List.of(
                                "minecraft:block_close",          //方块关闭
                                "minecraft:block_open",           //方块打开
                                "minecraft:container_close",      //容器关闭
                                "minecraft:container_open",       //容器打开
                                "minecraft:swim",                 //游泳
                                "minecraft:prime_fuse",           //引爆
                                "minecraft:explode",              //爆炸
                                "minecraft:lightning_strike",     //闪电
                                "minecraft:equip",                //穿上装备
                                "minecraft:unequip",              //卸下装备
                                "minecraft:projectile_land",      // 投掷物落地（关键）
                                "minecraft:projectile_shoot",     //弹射物发射
                                "minecraft:hit_ground",           // 实体落地
                                "minecraft:step",                 // 脚步声
                                "minecraft:block_place",          // 放置方块
                                "minecraft:block_destroy"         // 破坏方块
                        ),
                        s -> true);

// 新增：自定义吸引范围
        CUSTOM_ATTRACTION_RANGES = BUILDER
                .comment(
                        "Custom attraction ranges for specific GameEvents (in blocks).",
                        "Format: 'event_id' = range",
                        "If not set, uses the event's default notificationRadius.",
                        "Examples:",
                        "  'minecraft:projectile_land' = 16.0  (投掷物落地16格吸引范围)",
                        "  'minecraft:hit_ground' = 8.0       (实体落地8格吸引范围)"
                )
                .defineList("customAttractionRanges",
                        List.of(
                                "minecraft:step = 8.0",                  // 脚步声（原版16，这里限制为8）
                                "minecraft:swim = 6.0",                  // 游泳声
                                "minecraft:equip = 4.0",                 // 穿装备
                                "minecraft:unequip = 4.0",               // 卸装备
                                "minecraft:projectile_shoot = 3.0",      // 发射投掷物
                                "minecraft:hit_ground = 8.0",            // 实体落地
                                "minecraft:item_interact_finish = 8.0",  // 使用物品完成
                                "minecraft:projectile_land = 16.0",      // 投掷物落地
                                "minecraft:container_close = 16.0",      // 关闭容器
                                "minecraft:container_open = 16.0",       // 打开容器
                                "minecraft:block_place = 12.0",          // 放置方块
                                "minecraft:block_destroy = 12.0",        // 破坏方块
                                "minecraft:prime_fuse = 12.0",           // 点燃TNT
                                "minecraft:explode = 32.0",              // 爆炸
                                "minecraft:lightning_strike = 32.0"      // 闪电
                        ),
                        s -> true);

// 新增：静音护甲材质列表
        SILENT_ARMOR_MATERIALS = BUILDER
                .comment(
                        "Armor materials that allow silent movement when crouching.",
                        "Sound range calculation:",
                        "  1. No armor/Silent armor:",
                        "     - Walking: base range (e.g., 8 blocks)",
                        "     - Crouching: 1 block",
                        "  2. Heavy armor (not in list or mixed):",
                        "     - Walking: base range × 1.5 (e.g., 8 → 12 blocks)",
                        "     - Crouching: (base range × 1.5) × 0.5 (e.g., 8 → 12 → 6 blocks)",
                        "Supports:",
                        "  - Material names: 'leather', 'chainmail', 'iron', 'gold', 'diamond', 'netherite'",
                        "  - Mod materials: 'modid:material_name' (e.g., 'twilightforest:ironwood')",
                        "  - Item IDs: Full armor item IDs like 'minecraft:leather_chestplate'"
                )
                .defineList("silentArmorMaterials",
                        List.of(
                                "leather",           // 原版皮革
                                "wool",              // 如果有羊毛护甲mod
                                "cloth"              // 通用布料材质
                        ),
                        s -> true);

// 新增：振动遮挡开关
        ENABLE_VIBRATION_OCCLUSION = BUILDER
                .comment(
                        "Whether vibrations can be blocked by terrain.",
                        "Set to false to allow sounds to 'travel through walls'."
                )
                .define("enableVibrationOcclusion", false);

        BUILDER.pop();

        // === 新增原版AI集成配置 ===
        BUILDER.push("vanillaIntegration");
        BUILDER.comment(
                "Settings for integrating with vanilla Minecraft AI systems.",
                "⚠️ These settings affect how Aperi Oculos interacts with vanilla AI."
        );

        OVERRIDE_VANILLA_HAS_LINE_OF_SIGHT = BUILDER
                .comment(
                        "Override vanilla LivingEntity.hasLineOfSight() method to use Aperi Oculos vision system.",
                        "true = Use Aperi Oculos cached vision checks (recommended for performance)",
                        "false = Keep vanilla vision checks (may cause duplicate calculations)"
                )
                .define("overrideVanillaHasLineOfSight", true);

        DISABLE_VANILLA_TARGET_GOALS = BUILDER
                .comment(
                        "Automatically remove vanilla target selection AI goals from all mobs.",
                        "true = Remove goals like NearestAttackableTargetGoal (use with custom AI mods)",
                        "false = Keep vanilla target goals (default behavior)",
                        "⚠️ Only enable this if you're using a custom AI mod that handles targeting"
                )
                .define("disableVanillaTargetGoals", false);

        VANILLA_GOALS_WHITELIST = BUILDER
                .comment(
                        "Entity types that should keep their vanilla target goals (even if disableVanillaTargetGoals is true).",
                        "Supports entity IDs (e.g., 'minecraft:zombie') and tags (e.g., '#minecraft:undead').",
                        "This is useful for preserving vanilla behavior for specific mobs."
                )
                .defineList("vanillaGoalsWhitelist", List.of(), s -> true);

        BUILDER.pop();

        BUILDER.push("performance");
        BUILDER.comment("Settings to control the performance impact of the perception systems.");

        VISION_SCAN_RATE_TICKS = BUILDER
                .comment("How often (in game ticks) the active vision scan runs. Lower = more responsive but more intensive.")
                .defineInRange("visionScanRateTicks", 10, 1, 100);

        LINE_OF_SIGHT_CACHE_DURATION_TICKS = BUILDER
                .comment("How long (in game ticks) a line-of-sight check result is cached.")
                .defineInRange("lineOfSightCacheDurationTicks", 5, 0, 200);

        BUILDER.pop();

        // === 新增日志配置 ===
        BUILDER.push("logging");
        BUILDER.comment(
                "Logging settings for debugging and monitoring.",
                "⚠️ Enabling detailed logging may impact performance!"
        );

        ENABLE_DEBUG_LOGGING = BUILDER
                .comment("Master switch for all debug logging. Set to false to disable all logs.")
                .define("enableDebugLogging", false);

        LOG_VISION_EVENTS = BUILDER
                .comment("Log when TargetSpottedEvent is fired (observer sees target)")
                .define("logVisionEvents", true);

        LOG_VIBRATION_EVENTS = BUILDER
                .comment("Log when VibrationPerceivedEvent is fired (listener hears vibration)")
                .define("logVibrationEvents", true);

        LOG_CAPABILITY_OPERATIONS = BUILDER
                .comment("Log capability attach/detach and value changes")
                .define("logCapabilityOperations", false);

        LOG_CACHE_OPERATIONS = BUILDER
                .comment("Log cache hits/misses for line of sight checks")
                .define("logCacheOperations", false);

        LOG_PERFORMANCE_METRICS = BUILDER
                .comment("Log performance metrics (scan duration, entity count, etc.)")
                .define("logPerformanceMetrics", false);

        LOG_SAMPLING_RATE = BUILDER
                .comment(
                        "Log sampling rate: only log 1 out of every N events (1 = log all, 10 = log 10%)",
                        "Higher values reduce log spam but may miss important events"
                )
                .defineInRange("logSamplingRate", 1, 1, 1000);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}