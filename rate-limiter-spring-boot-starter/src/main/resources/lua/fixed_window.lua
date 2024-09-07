-- KEYS[1] 是服务的唯一标识符
-- ARGV[1] 是时间窗口大小（秒）
-- ARGV[2] 是限流阈值（请求次数）

local currentCount = redis.call('INCR', KEYS[1])

if tonumber(currentCount) == 1 then
    -- 第一次请求，设置过期时间
    redis.call('EXPIRE', KEYS[1], ARGV[1])
end

if tonumber(currentCount) > tonumber(ARGV[2]) then
    return 0  -- 请求被限流
else
    return 1  -- 请求被允许
end
