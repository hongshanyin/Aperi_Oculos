package io.github.Sorcery_Dynasties.aperioculos.capability;

/**
 * 听力能力接口
 * 用于存储实体的听力乘数（用于振动感知）
 */
public interface IHearingCapability {
    /**
     * 获取听力乘数
     * @return 乘数值（默认1.0）
     */
    double getHearingMultiplier();

    /**
     * 设置听力乘数
     * @param multiplier 乘数值（范围0.0-10.0）
     */
    void setHearingMultiplier(double multiplier);

    /**
     * 重置为默认值
     */
    void reset();
}