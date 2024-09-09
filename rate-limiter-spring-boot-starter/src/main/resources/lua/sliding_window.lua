-- KEYS[1] 是服务的唯一标识符
-- ARGV[1] 是窗口大小（秒）
-- ARGV[2] 是限流阈值（请求次数）
-- ARGV[3] 是当前请求的时间戳（毫秒级）-- 获取窗口开始时间戳
-- ARGV[4] uuid

-- 移除窗口开始时间之前的请求记录
local windowStart = tonumber(ARGV[3]) - tonumber(ARGV[1]) * 1000

-- 添加当前请求记录
redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, windowStart)

-- 获取窗口内的请求数量
redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4])

-- 判断是否超过限流阈值
local requestCount = redis.call('ZCARD', KEYS[1])

-- 如果超过阈值，删除刚添加的请求记录（模拟请求被拒绝）
if requestCount > tonumber(ARGV[2]) then
    redis.call('ZREM', KEYS[1], ARGV[4])
    -- 返回0表示请求被限流
    return 0
else
    -- 返回1表示请求被允许
    return 1
end