package io.github.Sorcery_Dynasties.aperioculos.systems;

import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * 原版AI集成系统
 * 根据配置移除原版的目标选择AI Goals
 * 这样可以避免与Aperi Oculos的视觉系统产生冲突和重复计算
 */
public class VanillaAIIntegration {

    /**
     * 使用FinalizeSpawn事件（在实体完全初始化后，goals已注册）
     * 优先级设置为LOWEST，确保在所有其他监听器之后执行
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMobFinalize(MobSpawnEvent.FinalizeSpawn event) {
        if (!(event.getLevel() instanceof ServerLevel)) {
            return;
        }

        if (!Config.DISABLE_VANILLA_TARGET_GOALS.get()) {
            return;
        }

        Mob mob = event.getEntity();

        if (isInWhitelist(mob)) {
            return;
        }

        removeVanillaTargetGoals(mob);
    }

    /**
     * 备用方案：使用EntityJoinLevel事件（对于非自然生成的实体，如刷怪笼/命令生成）
     * 优先级设置为LOWEST，确保goals已被注册
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!Config.DISABLE_VANILLA_TARGET_GOALS.get()) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (isInWhitelist(mob)) {
            return;
        }

        // 延迟1 tick执行，确保goals已初始化
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                if (mob.isAlive() && !mob.isRemoved()) {
                    removeVanillaTargetGoals(mob);
                }
            });
        }
    }

    /**
     * 移除原版的目标选择Goals
     */
    private void removeVanillaTargetGoals(Mob mob) {
        var goals = mob.targetSelector.getAvailableGoals().stream().toList();

        for (var wrappedGoal : goals) {
            Goal goal = wrappedGoal.getGoal();

            if (isTargetSelectionGoal(goal)) {
                mob.targetSelector.removeGoal(goal);
            }
        }
    }

    /**
     * 检查Goal是否是目标选择类型
     * 目标选择Goals通常继承自TargetGoal类
     */
    private boolean isTargetSelectionGoal(Goal goal) {
        if (goal instanceof TargetGoal) {
            return true;
        }

        if (goal instanceof NearestAttackableTargetGoal) {
            return true;
        }

        String className = goal.getClass().getSimpleName().toLowerCase();
        return className.contains("target") ||
               className.contains("attack") ||
               className.contains("nearest");
    }

    /**
     * 检查实体是否在白名单中
     */
    private boolean isInWhitelist(Mob mob) {
        EntityType<?> type = mob.getType();

        for (String entry : Config.VANILLA_GOALS_WHITELIST.get()) {
            if (entry.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(
                        Registries.ENTITY_TYPE,
                        ResourceLocation.parse(entry.substring(1))
                );
                if (type.is(tag)) {
                    return true;
                }
            } else {
                ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(type);
                if (Objects.equals(entityId, ResourceLocation.parse(entry))) {
                    return true;
                }
            }
        }

        return false;
    }
}
