package io.github.Sorcery_Dynasties.aperioculos.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

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
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEAF_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MONITORED_GAME_EVENTS;

    // === Performance Settings ===
    public static final ForgeConfigSpec.IntValue VISION_SCAN_RATE_TICKS;
    public static final ForgeConfigSpec.IntValue LINE_OF_SIGHT_CACHE_DURATION_TICKS;

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

        BUILDER.push("hearing");
        BUILDER.comment("Settings for vibration-based perception (GameEvent system).");

        DEFAULT_HEARING_MULTIPLIER = BUILDER
            .comment(
                "Default hearing multiplier for mobs (stored via Capability system).",
                "This value multiplies the GameEvent's notification radius."
            )
            .defineInRange("defaultHearingMultiplier", 1.0, 0.0, 10.0);

        DEAF_ENTITIES = BUILDER
            .comment("Entities that can never perceive vibrations through this framework.")
            .defineList("deafEntities", List.of(), s -> true);

        MONITORED_GAME_EVENTS = BUILDER
            .comment(
                "List of GameEvent IDs to monitor for vibration perception.",
                "Leave empty to monitor ALL GameEvents.",
                "Examples: 'minecraft:step', 'minecraft:projectile_shoot', 'minecraft:block_place'"
            )
            .defineList("monitoredGameEvents",
                List.of(
                    "minecraft:step",
                    "minecraft:swim",
                    "minecraft:projectile_shoot",
                    "minecraft:item_interact_finish",
                    "minecraft:block_place",
                    "minecraft:block_destroy"
                ),
                s -> true);

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

        SPEC = BUILDER.build();
    }
}
