-- KEYS[1] 代表 Redis 中存储的信号量键
-- ARGV[1] 代表要增加的信号量值

-- 将 KEYS[1] 对应的值增加 ARGV[1]，并将结果赋值给变量 value
local value = redis.call('incrby', KEYS[1], ARGV[1]);
return value