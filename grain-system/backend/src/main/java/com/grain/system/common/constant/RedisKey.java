package com.grain.system.common.constant;

/**
 * Redis Key 常量
 */
public interface RedisKey {

    // 用户Token: user:token:{userId} -> token字符串
    String USER_TOKEN = "user:token:";

    // 用户信息: user:info:{userId} -> LoginUser JSON
    String USER_INFO = "user:info:";

    // 权限缓存: perm:{userId} -> Set<String>
    String USER_PERM = "perm:";

    // 登录失败计数: login:fail:{username} -> count
    String LOGIN_FAIL_COUNT = "login:fail:";

    // 设备心跳: device:heartbeat:{deviceId} -> timestamp
    String DEVICE_HEARTBEAT = "device:heartbeat:";

    // 地磅实时数据: device:weigh:{deviceId} -> 最新称重JSON
    String DEVICE_WEIGH = "device:weigh:";

    // 储位环境数据: env:{positionId} -> 最新环境JSON
    String ENV_CURRENT = "env:current:";

    // 粮食种类列表缓存
    String GRAIN_TYPES = "config:grain_types";

    // 储位列表缓存
    String STORAGE_POSITIONS = "config:storage_positions";

    // 统计缓存: stats:{type}:{hash}
    String STATS_PREFIX = "stats:";

    // 收购单号序列: order:po:sequence -> Long
    String ORDER_PO_SEQUENCE = "order:po:sequence";

    // Token有效期（小时）
    long TOKEN_EXPIRE_HOURS = 8;

    // 基础配置缓存时间（分钟）
    long CONFIG_EXPIRE_MINUTES = 30;
}
