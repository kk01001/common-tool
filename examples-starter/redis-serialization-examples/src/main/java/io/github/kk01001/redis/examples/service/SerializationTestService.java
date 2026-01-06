package io.github.kk01001.redis.examples.service;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.redis.examples.entity.User;
import io.github.kk01001.redis.examples.vo.SerializationResultVO;
import org.nustaq.serialization.FSTConfiguration;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.ProtobufCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SerializationTestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationTestService.class);

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final Kryo kryo;
    private FSTConfiguration fstConf;

    public SerializationTestService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.objectMapper = new ObjectMapper();
        this.kryo = new Kryo();
        this.kryo.register(User.class);

        try {
            this.fstConf = FSTConfiguration.createDefaultConfiguration();
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize FST configuration. FST serialization will be disabled.", e);
            this.fstConf = null;
        }
    }

    public List<SerializationResultVO> testAllSerializations(int iterations) {
        List<SerializationResultVO> results = new ArrayList<>();
        User user = createTestUser();
        String uuid = UUID.randomUUID().toString();

        results.add(testStringSerialization(user, "test:string:" + uuid, iterations));
        results.add(testJsonSerialization(user, "test:json:" + uuid, iterations));
        results.add(testProtobufSerialization(user, "test:protobuf:" + uuid, iterations));
        results.add(testKryoSerialization(user, "test:kryo:" + uuid, iterations));
        // results.add(testKryo5Serialization(user, "test:kryo5:" + uuid, iterations));
        // results.add(testFstSerialization(user, "test:fst:" + uuid, iterations));

        return results;
    }

    private User createTestUser() {
        return new User(
                UUID.randomUUID().toString(),
                "张三",
                "zhangsan@example.com",
                30,
                "北京市朝阳区某某街道某某小区某某号楼某某单元某某室",
                "13800138000",
                "某某科技有限公司",
                "技术部",
                "高级工程师",
                "北京",
                "北京市",
                "中国",
                "100000",
                "110101199001011234",
                "6222021234567890123",
                "李四",
                "13900139000",
                "编程、阅读、旅游",
                "本科",
                "清华大学"
        );
    }

    private SerializationResultVO testStringSerialization(User user, String key, int iterations) {
        String codecType = "STRING";
        try {
            long totalWrite = 0, totalRead = 0, totalDelete = 0;
            long maxTime = Long.MIN_VALUE, minTime = Long.MAX_VALUE;

            for (int i = 0; i < iterations; i++) {
                String jsonString = objectMapper.writeValueAsString(user);
                RBucket<String> bucket = redissonClient.getBucket(key, org.redisson.client.codec.StringCodec.INSTANCE);

                long iterStart = System.nanoTime();

                long startWrite = System.nanoTime();
                bucket.set(jsonString);
                totalWrite += (System.nanoTime() - startWrite);

                long startRead = System.nanoTime();
                String retrieved = bucket.get();
                User deserializedUser = objectMapper.readValue(retrieved, User.class);
                totalRead += (System.nanoTime() - startRead);

                long startDelete = System.nanoTime();
                bucket.delete();
                totalDelete += (System.nanoTime() - startDelete);

                long iterTime = (System.nanoTime() - iterStart) / 1_000_000;
                maxTime = Math.max(maxTime, iterTime);
                minTime = Math.min(minTime, iterTime);
            }

            long writeMs = totalWrite / 1_000_000;
            long readMs = totalRead / 1_000_000;
            long deleteMs = totalDelete / 1_000_000;
            long totalMs = writeMs + readMs + deleteMs;
            long avgMs = totalMs / iterations;

            LOGGER.info("{} - Write: {}ms, Read: {}ms, Delete: {}ms, Total: {}ms, Max: {}ms, Min: {}ms, Avg: {}ms, Iterations: {}",
                    codecType, writeMs, readMs, deleteMs, totalMs, maxTime, minTime, avgMs, iterations);

            return new SerializationResultVO(codecType, writeMs, readMs, deleteMs, totalMs, iterations, maxTime, minTime, avgMs, true, null);
        } catch (Exception e) {
            LOGGER.error("Error testing {} serialization", codecType, e);
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false, e.getMessage());
        }
    }

    private SerializationResultVO testJsonSerialization(User user, String key, int iterations) {
        String codecType = "JSON";
        try {
            long totalWrite = 0, totalRead = 0, totalDelete = 0;
            long maxTime = Long.MIN_VALUE, minTime = Long.MAX_VALUE;

            for (int i = 0; i < iterations; i++) {
                RBucket<User> bucket = redissonClient.getBucket(key, new org.redisson.codec.JsonJacksonCodec());

                long iterStart = System.nanoTime();

                long startWrite = System.nanoTime();
                bucket.set(user);
                totalWrite += (System.nanoTime() - startWrite);

                long startRead = System.nanoTime();
                User retrieved = bucket.get();
                totalRead += (System.nanoTime() - startRead);

                long startDelete = System.nanoTime();
                bucket.delete();
                totalDelete += (System.nanoTime() - startDelete);

                long iterTime = (System.nanoTime() - iterStart) / 1_000_000;
                maxTime = Math.max(maxTime, iterTime);
                minTime = Math.min(minTime, iterTime);
            }

            long writeMs = totalWrite / 1_000_000;
            long readMs = totalRead / 1_000_000;
            long deleteMs = totalDelete / 1_000_000;
            long totalMs = writeMs + readMs + deleteMs;
            long avgMs = totalMs / iterations;

            LOGGER.info("{} - Write: {}ms, Read: {}ms, Delete: {}ms, Total: {}ms, Max: {}ms, Min: {}ms, Avg: {}ms, Iterations: {}",
                    codecType, writeMs, readMs, deleteMs, totalMs, maxTime, minTime, avgMs, iterations);

            return new SerializationResultVO(codecType, writeMs, readMs, deleteMs, totalMs, iterations, maxTime, minTime, avgMs, true, null);
        } catch (Exception e) {
            LOGGER.error("Error testing {} serialization", codecType, e);
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false, e.getMessage());
        }
    }

    private SerializationResultVO testProtobufSerialization(User user, String key, int iterations) {
        String codecType = "PROTOBUF";
        try {
            long totalWrite = 0, totalRead = 0, totalDelete = 0;
            long maxTime = Long.MIN_VALUE, minTime = Long.MAX_VALUE;

            for (int i = 0; i < iterations; i++) {
                RBucket<User> bucket = redissonClient.getBucket(key, new ProtobufCodec(User.class));
                long iterStart = System.nanoTime();

                long startWrite = System.nanoTime();
                bucket.set(user);
                totalWrite += (System.nanoTime() - startWrite);

                long startRead = System.nanoTime();
                User retrieved = bucket.get();
                totalRead += (System.nanoTime() - startRead);

                long startDelete = System.nanoTime();
                bucket.delete();
                totalDelete += (System.nanoTime() - startDelete);

                long iterTime = (System.nanoTime() - iterStart) / 1_000_000;
                maxTime = Math.max(maxTime, iterTime);
                minTime = Math.min(minTime, iterTime);
            }

            long writeMs = totalWrite / 1_000_000;
            long readMs = totalRead / 1_000_000;
            long deleteMs = totalDelete / 1_000_000;
            long totalMs = writeMs + readMs + deleteMs;
            long avgMs = totalMs / iterations;

            LOGGER.info("{} - Write: {}ms, Read: {}ms, Delete: {}ms, Total: {}ms, Max: {}ms, Min: {}ms, Avg: {}ms, Iterations: {}",
                    codecType, writeMs, readMs, deleteMs, totalMs, maxTime, minTime, avgMs, iterations);

            return new SerializationResultVO(codecType, writeMs, readMs, deleteMs, totalMs, iterations, maxTime, minTime, avgMs, true, null);
        } catch (Exception e) {
            LOGGER.error("Error testing {} serialization", codecType, e);
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false, e.getMessage());
        }
    }

    private SerializationResultVO testKryoSerialization(User user, String key, int iterations) {
        String codecType = "KRYO";
        try {
            long totalWrite = 0, totalRead = 0, totalDelete = 0;
            long maxTime = Long.MIN_VALUE, minTime = Long.MAX_VALUE;

            for (int i = 0; i < iterations; i++) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output output = new Output(baos);
                kryo.writeObject(output, user);
                output.close();
                byte[] kryoBytes = baos.toByteArray();

                RBucket<byte[]> bucket = redissonClient.getBucket(key, new org.redisson.codec.KryoCodec());

                long iterStart = System.nanoTime();

                long startWrite = System.nanoTime();
                bucket.set(kryoBytes);
                totalWrite += (System.nanoTime() - startWrite);

                long startRead = System.nanoTime();
                byte[] retrieved = bucket.get();
                Input input = new Input(new ByteArrayInputStream(retrieved));
                User deserializedUser = kryo.readObject(input, User.class);
                input.close();
                totalRead += (System.nanoTime() - startRead);

                long startDelete = System.nanoTime();
                bucket.delete();
                totalDelete += (System.nanoTime() - startDelete);

                long iterTime = (System.nanoTime() - iterStart) / 1_000_000;
                maxTime = Math.max(maxTime, iterTime);
                minTime = Math.min(minTime, iterTime);
            }

            long writeMs = totalWrite / 1_000_000;
            long readMs = totalRead / 1_000_000;
            long deleteMs = totalDelete / 1_000_000;
            long totalMs = writeMs + readMs + deleteMs;
            long avgMs = totalMs / iterations;

            LOGGER.info("{} - Write: {}ms, Read: {}ms, Delete: {}ms, Total: {}ms, Max: {}ms, Min: {}ms, Avg: {}ms, Iterations: {}",
                    codecType, writeMs, readMs, deleteMs, totalMs, maxTime, minTime, avgMs, iterations);

            return new SerializationResultVO(codecType, writeMs, readMs, deleteMs, totalMs, iterations, maxTime, minTime, avgMs, true, null);
        } catch (Exception e) {
            LOGGER.error("Error testing {} serialization", codecType, e);
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false, e.getMessage());
        }
    }

    private SerializationResultVO testKryo5Serialization(User user, String key, int iterations) {
        String codecType = "KRYO5";
        try {
            long totalWrite = 0, totalRead = 0, totalDelete = 0;
            long maxTime = Long.MIN_VALUE, minTime = Long.MAX_VALUE;

            for (int i = 0; i < iterations; i++) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output output = new Output(baos);
                kryo.writeObject(output, user);
                output.close();
                byte[] kryoBytes = baos.toByteArray();

                RBucket<byte[]> bucket = redissonClient.getBucket(key, new org.redisson.codec.Kryo5Codec());

                long iterStart = System.nanoTime();

                long startWrite = System.nanoTime();
                bucket.set(kryoBytes);
                totalWrite += (System.nanoTime() - startWrite);

                long startRead = System.nanoTime();
                byte[] retrieved = bucket.get();
                Input input = new Input(new ByteArrayInputStream(retrieved));
                User deserializedUser = kryo.readObject(input, User.class);
                input.close();
                totalRead += (System.nanoTime() - startRead);

                long startDelete = System.nanoTime();
                bucket.delete();
                totalDelete += (System.nanoTime() - startDelete);

                long iterTime = (System.nanoTime() - iterStart) / 1_000_000;
                maxTime = Math.max(maxTime, iterTime);
                minTime = Math.min(minTime, iterTime);
            }

            long writeMs = totalWrite / 1_000_000;
            long readMs = totalRead / 1_000_000;
            long deleteMs = totalDelete / 1_000_000;
            long totalMs = writeMs + readMs + deleteMs;
            long avgMs = totalMs / iterations;

            LOGGER.info("{} - Write: {}ms, Read: {}ms, Delete: {}ms, Total: {}ms, Max: {}ms, Min: {}ms, Avg: {}ms, Iterations: {}",
                    codecType, writeMs, readMs, deleteMs, totalMs, maxTime, minTime, avgMs, iterations);

            return new SerializationResultVO(codecType, writeMs, readMs, deleteMs, totalMs, iterations, maxTime, minTime, avgMs, true, null);
        } catch (Exception e) {
            LOGGER.error("Error testing {} serialization", codecType, e);
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false, e.getMessage());
        }
    }

    private SerializationResultVO testFstSerialization(User user, String key, int iterations) {
        String codecType = "FST";

        if (fstConf == null) {
            LOGGER.warn("FST is not available.");
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false,
                    "FST not available on Java 21+. Use --add-opens java.base/java.lang=ALL-UNNAMED");
        }

        try {
            long totalWrite = 0, totalRead = 0, totalDelete = 0;
            long maxTime = Long.MIN_VALUE, minTime = Long.MAX_VALUE;

            for (int i = 0; i < iterations; i++) {
                byte[] fstBytes = fstConf.asByteArray(user);
                RBucket<byte[]> bucket = redissonClient.getBucket(key, new org.redisson.codec.FstCodec());

                long iterStart = System.nanoTime();

                long startWrite = System.nanoTime();
                bucket.set(fstBytes);
                totalWrite += (System.nanoTime() - startWrite);

                long startRead = System.nanoTime();
                byte[] retrieved = bucket.get();
                User deserializedUser = (User) fstConf.asObject(retrieved);
                totalRead += (System.nanoTime() - startRead);

                long startDelete = System.nanoTime();
                bucket.delete();
                totalDelete += (System.nanoTime() - startDelete);

                long iterTime = (System.nanoTime() - iterStart) / 1_000_000;
                maxTime = Math.max(maxTime, iterTime);
                minTime = Math.min(minTime, iterTime);
            }

            long writeMs = totalWrite / 1_000_000;
            long readMs = totalRead / 1_000_000;
            long deleteMs = totalDelete / 1_000_000;
            long totalMs = writeMs + readMs + deleteMs;
            long avgMs = totalMs / iterations;

            LOGGER.info("{} - Write: {}ms, Read: {}ms, Delete: {}ms, Total: {}ms, Max: {}ms, Min: {}ms, Avg: {}ms, Iterations: {}",
                    codecType, writeMs, readMs, deleteMs, totalMs, maxTime, minTime, avgMs, iterations);

            return new SerializationResultVO(codecType, writeMs, readMs, deleteMs, totalMs, iterations, maxTime, minTime, avgMs, true, null);
        } catch (Exception e) {
            LOGGER.error("Error testing {} serialization", codecType, e);
            return new SerializationResultVO(codecType, 0, 0, 0, 0, iterations, 0, 0, 0, false, e.getMessage());
        }
    }
}
