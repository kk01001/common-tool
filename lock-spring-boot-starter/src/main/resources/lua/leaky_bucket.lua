local key = KEYS[1] -- 漏桶的 key
local limit = tonumber(ARGV[1]) -- 漏桶的容量
local leak_rate = tonumber(ARGV[2]) -- 每秒漏出的速率
local current_time = tonumber(ARGV[3]) -- 当前时间（单位：秒）
local increment = tonumber(ARGV[4]) -- 本次请求的水量

-- 获取上次更新时间和当前水量
local last_time = tonumber(redis.call("HGET", key, "last_time"))
local water = tonumber(redis.call("HGET", key, "water"))

if not last_time then
    -- 初始情况下漏桶为空
    last_time = current_time
    water = 0
end

-- 计算自上次更新以来漏掉的水量
local leaked_water = ((current_time - last_time) / 1000) * leak_rate
water = math.max(0, water - leaked_water) -- 漏掉的水量不能超过当前水量

-- 更新上次的时间戳
redis.call("HSET", key, "last_time", current_time)

-- 判断桶中是否还有空间放入新的水量（即请求是否可以通过）
if water + increment <= limit then
    -- 桶中有空间，允许请求通过，并更新水量
    water = water + increment
    redis.call("HSET", key, "water", water)
    return 1 -- 返回 1 表示请求通过
else
    -- 桶满了，拒绝请求
    return 0 -- 返回 0 表示请求被拒绝
end
