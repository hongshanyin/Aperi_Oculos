package io.github.Sorcery_Dynasties.aperioculos.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Vision Settings
    public static final ForgeConfigSpec.DoubleValue VIEW_FIELD_ANGLE;
    public static final ForgeConfigSpec.IntValue MIN_STEALTH_LIGHT_LEVEL;
    public static final ForgeConfigSpec.IntValue MAX_STEALTH_LIGHT_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_VISION_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLIND_ENTITIES;

    // Hearing Settings
    public static final ForgeConfigSpec.DoubleValue DEFAULT_HEARING_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEAF_ENTITIES;

    // Performance Settings
    public static final ForgeConfigSpec.IntValue VISION_SCAN_RATE_TICKS;
    public static final ForgeConfigSpec.IntValue LINE_OF_SIGHT_CACHE_DURATION_TICKS;

    static {
        BUILDER.push("vision");
        BUILDER.comment("Settings related to how entities 'see' the world.");
        VIEW_FIELD_ANGLE = BUILDER.defineInRange("viewFieldAngle", 180.0, 0.0, 360.0);
        MIN_STEALTH_LIGHT_LEVEL = BUILDER.defineInRange("minStealthLightLevel", 0, 0, 15);
        MAX_STEALTH_LIGHT_LEVEL = BUILDER.defineInRange("maxStealthLightLevel", 7, 0, 15);
        NIGHT_VISION_ENTITIES = BUILDER.comment("Entities immune to light-level checks. Supports entity IDs (e.g., 'minecraft:creeper') and tags (e.g., '#minecraft:undead').")
                .defineList("nightVisionEntities", List.of("#minecraft:undead", "minecraft:spider", "minecraft:cave_spider", "minecraft:enderman"), s -> true);
        BLIND_ENTITIES = BUILDER.comment("Entities that can never 'see' targets through this framework.")
                .defineList("blindEntities", List.of("minecraft:warden"), s -> true);
        BUILDER.pop();

        BUILDER.push("hearing");
        DEFAULT_HEARING_MULTIPLIER = BUILDER.comment("The default 'aperioculos:hearing_multiplier' attribute value for mobs.", "This value multiplies the standard notification radius of a game event.")
                .defineInRange("defaultHearingMultiplier", 1.0, 0.0, 10.0);
        DEAF_ENTITIES = BUILDER.comment("Entities that can never 'hear' sounds through this framework.")
                .defineList("deafEntities", List.of(), s -> true);
        BUILDER.pop();

        BUILDER.push("performance");
        BUILDER.comment("Settings to control the performance impact of the perception systems.");
        VISION_SCAN_RATE_TICKS = BUILDER.comment("How often (in game ticks) the active vision scan runs. Lower is more responsive but more performance-intensive.")
                .defineInRange("visionScanRateTicks", 10, 1, 100);
        LINE_OF_SIGHT_CACHE_DURATION_TICKS = BUILDER.comment("How long (in game ticks) a line-of-sight check result is cached to avoid redundant ray tracing.")
                .defineInRange("lineOfSightCacheDurationTicks", 5, 0, 200);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}