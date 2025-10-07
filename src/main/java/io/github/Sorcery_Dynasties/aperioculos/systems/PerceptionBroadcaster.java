package io.github.Sorcery_Dynasties.aperioculos.systems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 感知广播系统
 * 提供了向特定实体主动广播感知事件的能力。
 * 主要用于与其他AI模组联动，或在特定游戏逻辑下触发生物的感知。
 *
 * @TODO 未来实现具体逻辑
 */
public class PerceptionBroadcaster {

    /**
     * 向一个或多个目标实体广播一个虚拟的振动事件。
     * 这会触发Aperi Oculos的听觉系统，就像一个真实的GameEvent发生了一样。
     *
     * @param level 实体所在的世界
     * @param targets 接收广播的目标实体列表
     * @param sourcePos 虚拟声音的来源位置
     * @param gameEvent 虚拟声音的GameEvent类型
     * @param sourceEntity 产生该虚拟声音的实体（可选）
     */
    public static void broadcastVibrationTo(ServerLevel level, java.util.List<LivingEntity> targets,
                                            Vec3 sourcePos, GameEvent gameEvent,
                                            @Nullable Entity sourceEntity) {
        // 未来实现：
        // 1. 遍历 'targets' 列表。
        // 2. 对每个目标实体，执行类似于 VibrationSystem 中的检查逻辑
        //    （距离、听力乘数、遮挡等）。
        // 3. 如果检查通过，直接为该实体发布一个 VibrationPerceivedEvent。
        //
        // 注意：此方法会绕过全局的GameEvent监听，实现更精确的目标定位。
    }

    /**
     * 向单个目标实体广播虚拟振动事件。
     */
    public static void broadcastVibrationTo(ServerLevel level, LivingEntity target,
                                            Vec3 sourcePos, GameEvent gameEvent,
                                            @Nullable Entity sourceEntity) {
        broadcastVibrationTo(level, java.util.List.of(target), sourcePos, gameEvent, sourceEntity);
    }
}