package com.spldeolin.beginningmind.core.cache;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import com.spldeolin.beginningmind.core.cache.util.ProtostuffSerializationUtils;
import com.spldeolin.beginningmind.core.util.Times;

/**
 * Redis操作工具
 * <p>
 * 使用StringRedisSerializer作为Key和String类型Value的序列器
 * 使用ProtostuffSerializationUtils作为非String类型Value的序列器
 *
 * @author Deolin 2018/06/27
 */
@Component
public class RedisCache2 {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final StringRedisSerializer stringSerializer = new StringRedisSerializer();

    private byte[] rawKey(String key) {
        Assert.notNull(key, "key should not be null.");
        return stringSerializer.serialize(key);
    }

    private byte[][] rawKeys(Collection<String> keys) {
        Assert.notEmpty(keys, "keys should not be empty.");
        final byte[][] rawKeys = new byte[keys.size()][];
        int i = 0;
        for (String key : keys) {
            rawKeys[i++] = rawKey(key);
        }
        return rawKeys;
    }

    private byte[] rawValue(Object value) {
        Assert.notNull(value, "Value should not be null.");
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof String) {
            return stringSerializer.serialize((String) value);
        }
        return ProtostuffSerializationUtils.serialize(value);
    }

    /*
        key相关操作
    */

    /**
     * 删除key
     */
    public void delete(String key) {
        final byte[] rawKey = rawKey(key);
        redisTemplate.execute(connection -> {
            connection.del(rawKey);
            return null;
        }, true);
    }

    /**
     * 批量删除key
     */
    public void delete(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        final byte[][] rawKeys = rawKeys(keys);
        redisTemplate.execute(connection -> {
            connection.del(rawKeys);
            return null;
        }, true);
    }

    /**
     * 序列化key
     */
    public byte[] dump(String key) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.dump(rawKey), true);
    }

    /**
     * 是否存在key
     */
    public Boolean hasKey(String key) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.exists(rawKey), true);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        final byte[] rawKey = rawKey(key);
        final long rawTimeout = TimeoutUtils.toMillis(timeout, unit);
        return redisTemplate.execute(connection -> {
            try {
                return connection.pExpire(rawKey, rawTimeout);
            } catch (Exception e) {
                // Driver may not support pExpire or we may be running on Redis 2.4
                return connection.expire(rawKey, TimeoutUtils.toSeconds(timeout, unit));
            }
        }, true);
    }

    /**
     * 设置过期时间
     */
    public Boolean expireAt(String key, LocalDateTime localDateTime) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> {
            try {
                return connection.pExpireAt(rawKey, Times.toUnixTimestamp(localDateTime) * 1000);
            } catch (Exception e) {
                return connection.expireAt(rawKey, Times.toUnixTimestamp(localDateTime));
            }
        }, true);
    }

    /**
     * 查找匹配的key
     */
    public Set<String> keys(String pattern) {
        final byte[] rawKey = rawKey(pattern);
        Set<byte[]> rawKeyBtyesSet = redisTemplate.execute(connection -> connection.keys(rawKey), true);
        Set<String> rawKeys = rawKeyBtyesSet.stream().map(stringSerializer::deserialize).collect(Collectors.toSet());
        return rawKeys;
    }

    /**
     * 将当前数据库的 key 移动到给定的数据库 db 当中
     */
    public Boolean move(String key, int dbIndex) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.move(rawKey, dbIndex), true);
    }

    /**
     * 移除 key 的过期时间，key 将持久保持
     */
    public Boolean persist(String key) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.persist(rawKey), true);
    }

    /**
     * 返回 key 的剩余的过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        final byte[] rawKey = rawKey(key);

        return redisTemplate.execute(connection -> {
            try {
                return connection.pTtl(rawKey, timeUnit);
            } catch (Exception e) {
                // Driver may not support pTtl or we may be running on Redis 2.4
                return connection.ttl(rawKey, timeUnit);
            }
        }, true);
    }

    /**
     * 返回 key 的剩余的过期时间
     */
    public Long getExpire(String key) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.ttl(rawKey), true);
    }

    /**
     * 从当前数据库中随机返回一个 key
     */
    public String randomKey() {
        byte[] rawKey = redisTemplate.execute(RedisKeyCommands::randomKey, true);
        return stringSerializer.deserialize(rawKey);
    }

    /**
     * 修改 key 的名称
     */
    public void rename(String oldKey, String newKey) {
        final byte[] rawOldKey = rawKey(oldKey);
        final byte[] rawNewKey = rawKey(newKey);
        redisTemplate.execute(connection -> {
            connection.rename(rawOldKey, rawNewKey);
            return null;
        }, true);
    }

    /**
     * 仅当 newkey 不存在时，将 oldKey 改名为 newkey
     */
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        final byte[] rawOldKey = rawKey(oldKey);
        final byte[] rawNewKey = rawKey(newKey);
        return redisTemplate.execute(connection -> connection.renameNX(rawOldKey, rawNewKey), true);
    }

    /**
     * 返回 key 所储存的值的类型
     */
    public DataType type(String key) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.type(rawKey), true);
    }

    /*
        string（对象）相关操作
    */

    /**
     * 设置指定 key 的值
     */
    public void set(String key, Object value) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        redisTemplate.execute(connection -> {
            connection.set(rawKey, rawValue);
            return null;
        }, true);
    }

    /**
     * 获取指定 key 的值
     */
    public <T> T get(String key, Class<T> clazz) {
        final byte[] rawKey = rawKey(key);
        byte[] rawValue = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.get(rawKey));
        if (rawValue == null) {
            return null;
        }
        return ProtostuffSerializationUtils.deserialize(rawValue, clazz);
    }

    /**
     * 返回 key 中字符串值的子字符
     */
    public String getRange(String key, long start, long end) {
        final byte[] rawKey = rawKey(key);
        byte[] rawValue = redisTemplate.execute(
                (RedisCallback<byte[]>) connection -> connection.getRange(rawKey, start, end));
        return stringSerializer.deserialize(rawValue);
    }

    /**
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
     */
    public <T> T getSet(String key, String newValue, Class<T> oldClass) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawNewValue = rawValue(newValue);
        byte[] rawOldValue = redisTemplate.execute(
                (RedisCallback<byte[]>) connection -> connection.getSet(rawKey, rawNewValue));
        return ProtostuffSerializationUtils.deserialize(rawOldValue, oldClass);
    }

    /**
     * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
     */
    public Boolean getBit(String key, long offset) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.getBit(rawKey, offset), true);
    }

    /**
     * 批量获取
     */
    public <T> List<T> multiGet(Collection<String> keys, Class<T> clazz) {
        final byte[][] rawKeys = rawKeys(keys);
        List<byte[]> rawValues = redisTemplate.execute(connection -> connection.mGet(rawKeys), true);
        List<T> values = rawValues.stream().map(v -> ProtostuffSerializationUtils.deserialize(v, clazz)).collect(
                Collectors.toList());
        return values;
    }

    /**
     * 设置ASCII码, 字符串'a'的ASCII码是97, 转为二进制是'01100001', 此方法是将二进制第offset位值变为value
     *
     * @param value 值,true为1, false为0
     */
    public boolean setBit(String key, long offset, boolean value) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.setBit(rawKey, offset, value), true);
    }

    /**
     * 将值 value 关联到 key ，并将 key 的过期时间设为 timeout
     *
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setEx(String key, Object value, long timeout, TimeUnit unit) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                potentiallyUsePsetEx(connection);
                return null;
            }

            public void potentiallyUsePsetEx(RedisConnection connection) {
                if (!TimeUnit.MILLISECONDS.equals(unit) || !failsafeInvokePsetEx(connection)) {
                    connection.setEx(rawKey, TimeoutUtils.toSeconds(timeout, unit), rawValue);
                }
            }

            private boolean failsafeInvokePsetEx(RedisConnection connection) {
                boolean failed = false;
                try {
                    connection.pSetEx(rawKey, timeout, rawValue);
                } catch (UnsupportedOperationException e) {
                    // in case the connection does not support pSetEx return false to allow fallback to other operation.
                    failed = true;
                }
                return !failed;
            }
        }, true);
    }

    /**
     * 只有在 key 不存在时设置 key 的值
     *
     * @return 之前已经存在返回false, 不存在返回true
     */
    public boolean setIfAbsent(String key, Object value) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        return redisTemplate.execute(connection -> connection.setNX(rawKey, rawValue), true);
    }

    /**
     * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
     *
     * @param offset 从指定位置开始覆写
     */
    public void setRange(String key, String value, long offset) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        redisTemplate.execute(connection -> {
            connection.setRange(rawKey, rawValue, offset);
            return null;
        }, true);
    }

    /**
     * 获取字符串的长度
     */
    public long size(String key) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.strLen(rawKey), true);
    }

    /**
     * 批量添加
     */
    public void multiSet(Map<String, Object> map) {
        final Map<byte[], byte[]> rawMap = new LinkedHashMap<>(map.size());
        for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
            rawMap.put(rawKey(entry.getKey()), rawValue(entry.getValue()));
        }
        redisTemplate.execute(connection -> {
            connection.mSet(rawMap);
            return null;
        }, true);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     *
     * @return 之前已经存在返回false, 不存在返回true
     */
    public boolean multiSetIfAbsent(Map<String, String> map) {
        final Map<byte[], byte[]> rawMap = new LinkedHashMap<>(map.size());
        for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
            rawMap.put(rawKey(entry.getKey()), rawValue(entry.getValue()));
        }
        return redisTemplate.execute(connection -> connection.mSetNX(rawMap), true);
    }

    /**
     * 让指定对象自增一个整数
     * <p>
     * 要求key指向的对象是数字类型
     */
    public Long incrBy(String key, long delta) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.incrBy(rawKey, delta), true);
    }

    /**
     * 让指定对象自增一个浮点数
     * <p>
     * 要求key指向的对象是数字类型
     */
    public Double incrByFloat(String key, double delta) {
        final byte[] rawKey = rawKey(key);
        return redisTemplate.execute(connection -> connection.incrBy(rawKey, delta), true);
    }

    /**
     * 追加到末尾
     */
    public Integer append(String key, String value) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawKey(value);
        return redisTemplate.execute(connection -> {
            final Long result = connection.append(rawKey, rawValue);
            return (result != null) ? result.intValue() : null;
        }, true);
    }

    /*
        hash相关操作
    */

    /**
     * 获取存储在哈希表中指定字段的值
     */
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawHashKey = rawKey(hashKey);
        byte[] rawValue = redisTemplate.execute(connection -> connection.hGet(rawKey, rawHashKey), true);
        return ProtostuffSerializationUtils.deserialize(rawValue, clazz);
    }

    /**
     * 获取所有给定字段的值
     */
    public Map<String, T> hGetAll(String key, Class<T> clazz) {
        final byte[] rawKey = rawKey(key);
        Map<byte[], byte[]> rawValuesByKey = redisTemplate.execute(connection -> connection.hGetAll(rawKey), true);
        Map<String, T> valuesByKey = new HashMap<>(rawValuesByKey.size());
        for (Map.Entry<byte[], byte[]> entry : rawValuesByKey.entrySet()) {
            String targetKey = stringSerializer.deserialize(entry.getKey());
            T targetValue = ProtostuffSerializationUtils.deserialize(entry.getValue(), clazz);
            valuesByKey.put(targetKey, targetValue);
        }
        return valuesByKey;
    }

    /**
     * 获取所有给定字段的值
     */
    public <T> List<T> hMultiGet(String key, Collection<String> hashKeys, Class<T> clazz) {
        final byte[] rawKey = rawKey(key);
        final byte[][] rawHashKeys = rawKeys(hashKeys);
        List<byte[]> rawValues = redisTemplate.execute(connection -> connection.hMGet(rawKey, rawHashKeys), true);
        List<T> values = rawValues.stream().map(v -> ProtostuffSerializationUtils.deserialize(v, clazz)).collect(
                Collectors.toList());
        return values;
    }

    public void hPut(String key, String hashKey, String value) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawHashKey = rawKey(hashKey);
        final byte[] rawValue = rawValue(value);
        redisTemplate.execute(connection -> {
            connection.hSet(rawKey, rawHashKey, rawValue);
            return null;
        }, true);
    }

    public void hPutAll(String key, Map<String, Object> valuesByKey) {
        if (valuesByKey.isEmpty()) {
            return;
        }
        final byte[] rawKey = rawKey(key);
        final Map<byte[], byte[]> rawValuesByKey = new LinkedHashMap<>(valuesByKey.size());
        for (Map.Entry<String, Object> valueByKey : valuesByKey.entrySet()) {
            byte[] rawHashKey = rawKey(valueByKey.getKey());
            byte[] rawValue = rawValue(valueByKey.getValue());
            rawValuesByKey.put(rawHashKey, rawValue);
        }
        redisTemplate.execute(connection -> {
            connection.hMSet(rawKey, rawValuesByKey);
            return null;
        }, true);
    }

    /**
     * 仅当hashKey不存在时才设置
     */
    public Boolean hPutIfAbsent(String key, String hashKey, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * 删除一个或多个哈希表字段
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     */
    public boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     */
    public Long hIncrBy(String key, Object field, long increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     */
    public Double hIncrByFloat(String key, Object field, double delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * 获取所有哈希表中的字段
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取哈希表中字段的数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 获取哈希表中所有值
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 迭代哈希表中的键值对
     */
    public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
        return redisTemplate.opsForHash().scan(key, options);
    }

    /*
        list相关操作
    */

    /**
     * 通过索引获取列表中的元素
     */
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 获取列表指定范围内的元素
     *
     * @param start 开始位置, 0是开始位置
     * @param end 结束位置, -1返回所有
     */
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 存储在list头部
     */
    public Long lLeftPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Long lLeftPushAll(String key, String... value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    public Long lLeftPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 当list存在的时候才加入
     */
    public Long lLeftPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    /**
     * 如果pivot存在,再pivot前面添加
     */
    public Long lLeftPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    public Long lRightPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Long lRightPushAll(String key, String... value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    public Long lRightPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 为已存在的列表添加值
     */
    public Long lRightPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    /**
     * 在pivot元素的右边添加值
     */
    public Long lRightPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    /**
     * 通过索引设置列表元素的值
     *
     * @param index 位置
     */
    public void lSet(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @return 删除的元素
     */
    public String lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param timeout 等待时间
     * @param unit 时间单位
     */
    public String lBLeftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    /**
     * 移除并获取列表最后一个元素
     *
     * @return 删除的元素
     */
    public String lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param timeout 等待时间
     * @param unit 时间单位
     */
    public String lBRightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     */
    public String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
                destinationKey);
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     */
    public String lBRightPopAndLeftPush(String sourceKey, String destinationKey,
            long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
                destinationKey, timeout, unit);
    }

    /**
     * 删除集合中值等于value得元素
     *
     * @param index index=0, 删除所有值等于value的元素; index>0, 从头部开始删除第一个值等于value的元素;
     * index<0, 从尾部开始删除第一个值等于value的元素;
     */
    public Long lRemove(String key, long index, String value) {
        return redisTemplate.opsForList().remove(key, index, value);
    }

    /**
     * 裁剪list
     */
    public void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     */
    public Long lLen(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /*
        set相关操作
    */

    /**
     * set添加元素
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * set移除元素
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 移除并返回集合的一个随机元素
     */
    public String sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * 将元素value从一个集合移到另一个集合
     */
    public Boolean sMove(String key, String value, String destKey) {
        return redisTemplate.opsForSet().move(key, value, destKey);
    }

    /**
     * 获取集合的大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 判断集合是否包含value
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取两个集合的交集
     */
    public Set<String> sIntersect(String key, String otherKey) {
        return redisTemplate.opsForSet().intersect(key, otherKey);
    }

    /**
     * 获取key集合与多个集合的交集
     */
    public Set<String> sIntersect(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().intersect(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的交集存储到destKey集合中
     */
    public Long sIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKey,
                destKey);
    }

    /**
     * key集合与多个集合的交集存储到destKey集合中
     */
    public Long sIntersectAndStore(String key, Collection<String> otherKeys,
            String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKeys,
                destKey);
    }

    /**
     * 获取两个集合的并集
     */
    public Set<String> sUnion(String key, String otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 获取key集合与多个集合的并集
     */
    public Set<String> sUnion(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的并集存储到destKey中
     */
    public Long sUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的并集存储到destKey中
     */
    public Long sUnionAndStore(String key, Collection<String> otherKeys,
            String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的差集
     */
    public Set<String> sDifference(String key, String otherKey) {
        return redisTemplate.opsForSet().difference(key, otherKey);
    }

    /**
     * 获取key集合与多个集合的差集
     */
    public Set<String> sDifference(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().difference(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的差集存储到destKey中
     */
    public Long sDifference(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKey,
                destKey);
    }

    /**
     * key集合与多个集合的差集存储到destKey中
     */
    public Long sDifference(String key, Collection<String> otherKeys,
            String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKeys,
                destKey);
    }

    /**
     * 获取集合所有元素
     */
    public Set<String> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 随机获取集合中的一个元素
     */
    public String sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 随机获取集合中count个元素
     */
    public List<String> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    /**
     * 随机获取集合中count个元素并且去除重复的
     */
    public Set<String> sDistinctRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    public Cursor<String> sScan(String key, ScanOptions options) {
        return redisTemplate.opsForSet().scan(key, options);
    }

    /*
        zSet相关操作
    */

    /**
     * 添加元素,有序集合是按照元素的score值由小到大排列
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Long zAdd(String key, Set<TypedTuple<String>> values) {
        return redisTemplate.opsForZSet().add(key, values);
    }

    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 增加元素的score值，并返回增加后的值
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
     *
     * @return 0表示第一位
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 返回元素在集合的排名,按元素的score值由大到小排列
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取集合的元素, 从小到大排序
     *
     * @param start 开始位置
     * @param end 结束位置, -1查询所有
     */
    public Set<String> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取集合元素, 并且把score值也获取
     */
    public Set<TypedTuple<String>> zRangeWithScores(String key, long start,
            long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 根据Score值查询集合元素
     *
     * @param min 最小值
     * @param max 最大值
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从小到大排序
     *
     * @param min 最小值
     * @param max 最大值
     */
    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key,
            double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key,
            double min, double max, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max,
                start, end);
    }

    /**
     * 获取集合的元素, 从大到小排序
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取集合的元素, 从大到小排序, 并返回score值
     */
    public Set<TypedTuple<String>> zReverseRangeWithScores(String key,
            long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start,
                end);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     */
    public Set<String> zReverseRangeByScore(String key, double min,
            double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     */
    public Set<TypedTuple<String>> zReverseRangeByScoreWithScores(
            String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key,
                min, max);
    }

    public Set<String> zReverseRangeByScore(String key, double min,
            double max, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max,
                start, end);
    }

    /**
     * 根据score值获取集合元素数量
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取集合大小
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取集合大小
     */
    public Long zZCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取集合中value元素的score值
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 移除指定索引位置的成员
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 根据指定的score值的范围来移除成员
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 获取key和otherKey的并集并存储在destKey中
     */
    public Long zUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    public Long zUnionAndStore(String key, Collection<String> otherKeys,
            String destKey) {
        return redisTemplate.opsForZSet()
                .unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 交集
     */
    public Long zIntersectAndStore(String key, String otherKey,
            String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKey,
                destKey);
    }

    /**
     * 交集
     */
    public Long zIntersectAndStore(String key, Collection<String> otherKeys,
            String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys,
                destKey);
    }

    public Cursor<TypedTuple<String>> zScan(String key, ScanOptions options) {
        return redisTemplate.opsForZSet().scan(key, options);
    }
}