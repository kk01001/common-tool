-- KEYS[1] 是令牌桶的标识符
-- ARGV[1] 是桶的容量（最大令牌数）
-- ARGV[2] 是令牌生成速率（每秒生成的令牌数）
-- ARGV[3] 是当前时间戳（毫秒级）

local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- 获取当前令牌数及上次更新时间
local bucket = redis.call('HMGET', KEYS[1], 'tokens', 'lastTime')
local tokens = tonumber(bucket[1])
local lastTime = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    lastTime = now
end

-- 计算自上次请求以来产生的令牌数
local delta = (now - lastTime) / 1000 * rate
tokens = math.min(capacity, tokens + delta)

if tokens < 1 then
    return 0  -- 没有足够的令牌，请求被限流
else
    -- 消耗一个令牌并更新令牌数及时间
    redis.call('HSET', KEYS[1], 'tokens', tokens - 1, 'lastTime', now)
    return 1  -- 请求被允许
end
