package io.github.Sorcery_Dynasties.aperioculos.util;

import io.github.Sorcery_Dynasties.aperioculos.config.Config;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * 护甲音效辅助类
 * 处理基于护甲的声音范围修正
 */
public class ArmorSoundHelper {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    /**
     * 获取实体的声音范围修正系数
     *
     * @param entity 要检查的实体
     * @param isCrouching 实体是否在潜行
     * @param baseRange 基础声音范围
     * @return 修正后的声音范围
     */
    public static double getModifiedSoundRange(LivingEntity entity, boolean isCrouching, double baseRange) {
        ArmorType armorType = getArmorType(entity);

        double modifiedRange = baseRange;

        // 第一步：护甲修正（正常移动时）
        if (armorType == ArmorType.HEAVY) {
            modifiedRange = baseRange * 1.5; // 重甲：增加50%
        }

        // 第二步：潜行修正（在护甲修正后的基础上）
        if (isCrouching) {
            if (armorType == ArmorType.SILENT || armorType == ArmorType.NONE) {
                return 1.0; // 静音护甲或无护甲潜行：固定1格
            } else if (armorType == ArmorType.HEAVY) {
                return modifiedRange * 0.5; // 重甲潜行：在增加50%后再减半
                // 例如：baseRange=8 → 重甲12 → 潜行6
            }
        }

        return modifiedRange;
    }

    /**
     * 判断实体的护甲类型
     *
     * @param entity 要检查的实体
     * @return 护甲类型枚举
     */
    public static ArmorType getArmorType(LivingEntity entity) {
        List<? extends String> silentMaterials = Config.SILENT_ARMOR_MATERIALS.get();

        boolean hasAnyArmor = false;
        boolean hasHeavyArmor = false;
        boolean allSilent = true;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = entity.getItemBySlot(slot);

            if (!armorStack.isEmpty()) {
                hasAnyArmor = true;

                if (!isSilentArmor(armorStack, silentMaterials)) {
                    hasHeavyArmor = true;
                    allSilent = false;
                }
            }
        }

        if (!hasAnyArmor) {
            return ArmorType.NONE; // 无护甲
        } else if (hasHeavyArmor) {
            return ArmorType.HEAVY; // 有任何重甲 → 视为重甲
        } else if (allSilent) {
            return ArmorType.SILENT; // 全部是静音护甲
        } else {
            return ArmorType.NONE; // 理论上不会到这里
        }
    }

    /**
     * 检查单件护甲是否在静音列表中
     *
     * @param armorStack 护甲物品
     * @param silentMaterials 静音材质列表
     * @return true 如果这件护甲是静音的
     */
    private static boolean isSilentArmor(ItemStack armorStack, List<? extends String> silentMaterials) {
        if (!(armorStack.getItem() instanceof ArmorItem armorItem)) {
            return true; // 非护甲物品（理论上不会出现）
        }

        // 方法1: 通过护甲材质名称匹配（优先）
        String materialName = armorItem.getMaterial().toString().toLowerCase();
        for (String silentMaterial : silentMaterials) {
            if (materialName.contains(silentMaterial.toLowerCase())) {
                return true;
            }
        }

        // 方法2: 通过完整物品ID匹配（支持 'minecraft:leather_chestplate' 格式）
        String itemId = ForgeRegistries.ITEMS.getKey(armorStack.getItem()).toString();
        for (String silentMaterial : silentMaterials) {
            if (itemId.equalsIgnoreCase(silentMaterial)) {
                return true;
            }
        }

        // 方法3: 通过物品ID的材质部分匹配（例如 'leather' 匹配 'minecraft:leather_boots'）
        for (String silentMaterial : silentMaterials) {
            if (itemId.contains(silentMaterial.toLowerCase())) {
                return true;
            }
        }

        return false; // 不在静音列表中
    }

    /**
     * 获取实体当前穿戴的护甲材质列表（用于调试）
     *
     * @param entity 要检查的实体
     * @return 护甲材质字符串列表
     */
    public static String getArmorMaterialsDebug(LivingEntity entity) {
        StringBuilder sb = new StringBuilder("[");

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = entity.getItemBySlot(slot);

            if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem armorItem) {
                if (sb.length() > 1) sb.append(", ");
                sb.append(armorItem.getMaterial().toString());
            }
        }

        sb.append("] -> ");
        sb.append(getArmorType(entity));
        return sb.toString();
    }

    /**
     * 护甲类型枚举
     */
    public enum ArmorType {
        NONE,    // 无护甲
        SILENT,  // 全套静音护甲（皮革等）
        HEAVY    // 重甲或混穿
    }
}
