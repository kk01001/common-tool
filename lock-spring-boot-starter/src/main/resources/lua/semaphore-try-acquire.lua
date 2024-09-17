-- KEYS[1] 代表 Redis 中存储信号量的键
-- ARGV[1] 代表请求需要获取的信号量数量
-- ARGV[2] 代表信号量的上限

-- 首先检查信号量键是否存在，如果不存在则进行初始化设置上限
local value = redis.call('get', KEYS[1]);
if (value == false) then
    -- 初始化信号量为 ARGV[2]（上限值）
    redis.call('set', KEYS[1], ARGV[2]);
    -- 初始化后的值
    value = ARGV[2];
end;

-- 判断键是否存在并且值是否大于或等于 ARGV[1]（需要的信号量数量）
if (tonumber(value) >= tonumber(ARGV[1])) then
    -- 如果满足条件，使用 decrby 减少信号量 ARGV[1]，并返回 1 表示成功
    redis.call('decrby', KEYS[1], ARGV[1]);
    return 1;
end;

-- 如果不满足条件，返回 0 表示获取信号量失败
return 0;
