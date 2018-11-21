package cwh.order.customer.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Component
public class IdWorker {

    private long workerId = 0L;
    private long datacenterId = 0L;
    private long sequence = 0L;

    private long twepoch = 1288834974657L;

    private long workerIdBits = 5L;
    private long datacenterIdBits = 5L;
    //    private long maxWorkerId = ~(-1L << workerIdBits);
//    private long maxDatacenterId = ~(-1L << datacenterIdBits);
    private long sequenceBits = 12L;

    private long workerIdShift = sequenceBits;
    private long datacenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private long sequenceMask = ~(-1L << sequenceBits);

    private long lastTimestamp = -1L;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

//    public IdWorker(long workerId, long datacenterId) {
//        // sanity check for workerId
//        if (workerId > maxWorkerId || workerId < 0) {
//            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
//        }
//        if (datacenterId > maxDatacenterId || datacenterId < 0) {
//            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
//        }
//        this.workerId = workerId;
//        this.datacenterId = datacenterId;
//    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public void lock(String table_id, String uniqueId) {
        String lock_key = table_id + Constant.separator + "lock";
        long max_time = 800;
        long waitTime = 0;
        while (waitTime < max_time) {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lock_key, uniqueId, max_time, TimeUnit.MILLISECONDS);
            if (result != null && result) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitTime += 10;
            }
        }
        if (waitTime == max_time) {
            redisTemplate.opsForValue().set(lock_key, uniqueId, max_time, TimeUnit.MILLISECONDS);
        }
    }

    public void unLock(String table_id, String uniqueId) {
        String lock_key = table_id + Constant.separator + "lock";
        String result = redisTemplate.opsForValue().get(lock_key);
        if (result != null && result.equals(uniqueId)) {
            redisTemplate.delete(lock_key);
        }
    }
}

