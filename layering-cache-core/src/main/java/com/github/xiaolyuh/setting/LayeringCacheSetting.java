package com.github.xiaolyuh.setting;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 多级缓存配置项
 *
 * @author yuhao.wang
 */
public class LayeringCacheSetting implements Serializable {
    private static final String SPLIT = "-";
    /**
     * 内部缓存名，由[一级缓存有效时间-二级缓存有效时间-二级缓存自动刷新时间]组成
     */
    private String internalKey;

    /**
     * 描述，数据监控页面使用
     */
    private String depict;

    /**
     * 是否使用一级缓存
     */
    boolean useFirstCache = true;

    /**
     * 是否允许存NULL值
     */
    boolean allowNullValue = false;

    /**
     * 非空值和null值之间的时间倍率，默认是1。allowNullValue=true才有效
     * <p>
     * 如配置缓存的有效时间是200秒，倍率这设置成10，
     * 那么当缓存value为null时，缓存的有效时间将是20秒，非空时为200秒
     * <p/>
     */
    int magnification = 1;

    /**
     * 一级缓存配置
     */
    private FirstCacheSetting firstCacheSetting;

    /**
     * 二级缓存配置
     */
    private SecondaryCacheSetting secondaryCacheSetting;

    public LayeringCacheSetting() {
    }

    public LayeringCacheSetting(FirstCacheSetting firstCacheSetting, SecondaryCacheSetting secondaryCacheSetting,
                                String depict, boolean isAllowNullValue, int magnification) {
        this.firstCacheSetting = firstCacheSetting;
        this.secondaryCacheSetting = secondaryCacheSetting;
        this.depict = depict;
        this.allowNullValue = isAllowNullValue;
        this.magnification = magnification;
        internalKey();
    }

    @JSONField(serialize = false, deserialize = false)
    private void internalKey() {
        // 一级缓存有效时间-二级缓存有效时间-二级缓存自动刷新时间
        StringBuilder sb = new StringBuilder();
        if (firstCacheSetting != null) {
            sb.append(firstCacheSetting.getTimeUnit().toMillis(firstCacheSetting.getExpireTime()));
        }
        sb.append(SPLIT);
        if (secondaryCacheSetting != null) {
            sb.append(secondaryCacheSetting.getTimeUnit().toMillis(secondaryCacheSetting.getExpiration()));
            sb.append(SPLIT);
            sb.append(secondaryCacheSetting.getTimeUnit().toMillis(secondaryCacheSetting.getPreloadTime()));
        }
        internalKey = sb.toString();
    }

    public FirstCacheSetting getFirstCacheSetting() {
        return firstCacheSetting;
    }

    public SecondaryCacheSetting getSecondaryCacheSetting() {
        return secondaryCacheSetting;
    }

    public String getInternalKey() {
        return internalKey;
    }

    public void internalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    public boolean isUseFirstCache() {
        return useFirstCache;
    }

    public void setUseFirstCache(boolean useFirstCache) {
        this.useFirstCache = useFirstCache;
    }

    public void setFirstCacheSetting(FirstCacheSetting firstCacheSetting) {
        this.firstCacheSetting = firstCacheSetting;
    }

    public void setSecondaryCacheSetting(SecondaryCacheSetting secondaryCacheSetting) {
        this.secondaryCacheSetting = secondaryCacheSetting;
    }

    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public boolean getAllowNullValue() {
        return allowNullValue;
    }

    public void setAllowNullValue(boolean allowNullValue) {
        this.allowNullValue = allowNullValue;
    }

    public int getMagnification() {
        return magnification;
    }

    public void setMagnification(int magnification) {
        this.magnification = magnification;
    }
}
