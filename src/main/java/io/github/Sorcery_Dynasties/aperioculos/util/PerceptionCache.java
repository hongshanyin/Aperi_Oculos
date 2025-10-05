package io.github.Sorcery_Dynasties.aperioculos.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Pair;
import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PerceptionCache {
    // A cache for Line of Sight (LoS) checks.
    // Key: A pair of (Observer UUID, Target UUID)
    // Value: Boolean (true if LoS is clear, false if blocked)
    public static final Cache<Pair<UUID, UUID>, Boolean> LINE_OF_SIGHT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Config.LINE_OF_SIGHT_CACHE_DURATION_TICKS.get() * 50L, TimeUnit.MILLISECONDS)
            .build();
}