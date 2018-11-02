package com.testwa.core.redis;

import com.alibaba.fastjson.JSON;
import com.testwa.core.redis.config.RedisHAClientConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.SafeEncoder;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * 封装Jedis API，提供redis命令调用的操作
 */
public class RedisClient implements RedisOperation {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String cacheName = "default";

    private String redisServerHost = "localhost";

    private int redisServerPort = Protocol.DEFAULT_PORT;

    private String redisAuthKey;

    private JedisPool jedisPool;

    private boolean isAlive = true;

    private int timeout = Protocol.DEFAULT_TIMEOUT;

    private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;

    private long maxWait = GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;

    private boolean testOnBorrow = GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW;

    private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

    private boolean testOnReturn = GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN;


    /**
     * Creates a new instance of RedisClient.
     */
    public RedisClient(RedisHAClientConfig clientConfig) {
        if (clientConfig == null) {
            throw new IllegalArgumentException("Client config is null");
        }
        this.cacheName = clientConfig.getCacheName();
        this.redisServerHost = clientConfig.getRedisServerHost();
        this.redisServerPort = clientConfig.getRedisServerPort();
        this.timeout = clientConfig.getTimeout();
        this.redisAuthKey = clientConfig.getRedisAuthKey();
        if (StringUtils.isEmpty(redisAuthKey)) {
            logger.info("use no auth mode for " + redisServerHost);
            jedisPool = new JedisPool(getPoolConfig(), redisServerHost, redisServerPort, timeout);
        } else {
            jedisPool = new JedisPool(getPoolConfig(), redisServerHost, redisServerPort, timeout, redisAuthKey);
        }
        onAfterInit(redisServerHost, redisServerPort);
    }

    protected void onAfterInit(String host, int port) {
        logger.info("New Jedis pool <client: " + cacheName + "> <server: " + this.getLiteralRedisServer() +
                "> object created. Connection pool will be initiated when calling.");
    }

    private GenericObjectPoolConfig getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        // maxIdle为负数时，不对pool size大小做限制，此处做限制，防止保持过多空闲redis连接
        if (this.maxIdle >= 0) {
            poolConfig.setMaxTotal(this.maxIdle);
        }
        poolConfig.setMaxWaitMillis(this.maxWait);
        poolConfig.setMaxIdle(this.maxIdle);
        poolConfig.setMaxWaitMillis(this.maxWait);
        poolConfig.setTestOnBorrow(this.testOnBorrow);
        // 在还回给pool时，是否提前进行validate操作
        poolConfig.setTestOnReturn(this.testOnReturn);
        return poolConfig;
    }

    public String ping() {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            String pong = jedis.ping();
            return pong;
        } catch (Exception e) {
            logger.debug(e.getMessage());
            if(jedis != null){
                jedis.close();
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * get old value and set new value
     *
     * @param key
     * @param value
     * @param expiration
     *
     * @return false if redis did not execute the option
     *
     * @throws Exception
     * @author wangchongjie
     */
    public Object getSet(String key, Object value, Integer expiration) throws Exception {
        Jedis jedis = null;

        try {

            jedis = this.jedisPool.getResource();
            long begin = System.currentTimeMillis();
            // 操作expire成功返回1，失败返回0，仅当均返回1时，实际操作成功
            byte[] val = jedis.getSet(SafeEncoder.encode(key), serialize(value));
            Object result = deserialize(val);

            boolean success = true;
            if (expiration > 0) {
                Long res = jedis.expire(key, expiration);
                if (res == 0L) {
                    success = false;
                }
            }
            long end = System.currentTimeMillis();
            if (success) {
                logger.info("getset key:" + key + ", spends: " + (end - begin) + "ms");
            } else {
                logger.info("getset key: " + key + " failed, key has already exists! ");
            }

            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * get value<br>
     * return null if key did not exist
     *
     * @param key
     *
     * @return
     *
     * @throws Exception
     */
    public Object get(String key) throws Exception {
        byte[] data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            long begin = System.currentTimeMillis();
            data = jedis.get(SafeEncoder.encode(key));
            long end = System.currentTimeMillis();
            logger.info("get key:" + key + ", spends: " + (end - begin) + "ms");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return this.deserialize(data);
    }

    /**
     * value set<br>
     * The string can't be longer than 1073741824 bytes (1 GB).
     *
     * @param key
     * @param value
     * @param expiration
     *
     * @return false if redis did not execute the option
     */
    public boolean set(String key, Object value, Integer expiration) throws Exception {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();

            long begin = System.currentTimeMillis();
            if (expiration > 0) {
                result = jedis.setex(SafeEncoder.encode(key), expiration, serialize(value));
            } else {
                result = jedis.set(SafeEncoder.encode(key), serialize(value));
            }
            long end = System.currentTimeMillis();
            logger.info("set key:" + key + ", spends: " + (end - begin) + "ms");
            return "OK".equalsIgnoreCase(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    /**
     * set a value without expiration
     *
     * @param key
     * @param value
     *
     * @return false if redis did not execute the option
     *
     * @throws Exception
     */
    public boolean set(String key, Object value) throws Exception {
        return this.set(key, value, -1);
    }

    /**
     * add if not exists
     *
     * @param key
     * @param value
     * @param expiration
     *
     * @return false if redis did not execute the option
     *
     * @throws Exception
     */
    public boolean add(String key, Object value, Integer expiration) throws Exception {
        Jedis jedis = null;

        try {

            jedis = this.jedisPool.getResource();
            long begin = System.currentTimeMillis();
            // 操作setnx与expire成功返回1，失败返回0，仅当均返回1时，实际操作成功
            Long result = jedis.setnx(SafeEncoder.encode(key), serialize(value));
            if (expiration > 0) {
                result = result & jedis.expire(key, expiration);
            }
            long end = System.currentTimeMillis();
            if (result == 1L) {
                logger.info("add key:" + key + ", spends: " + (end - begin) + "ms");
            } else {
                logger.info("add key: " + key + " failed, key has already exists! ");
            }

            return result == 1L;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * add if not exists
     *
     * @param key
     * @param value
     *
     * @return false if redis did not execute the option
     *
     * @throws Exception
     */
    public boolean add(String key, Object value) throws Exception {
        return this.add(key, value, -1);
    }

    /**
     * Test if the specified key exists.
     *
     * @param key
     *
     * @return
     *
     * @throws Exception
     */
    public boolean exists(String key) throws Exception {
        boolean isExist = false;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            isExist = jedis.exists(SafeEncoder.encode(key));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return isExist;
    }

    public Set<String> keys(String pattern) {
        Set<String> data = Collections.EMPTY_SET;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            data = jedis.keys(pattern);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }

    /**
     * Remove the specified keys.
     *
     * @param key
     *
     * @return false if redis did not execute the option
     */
    public boolean delete(String key) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.del(SafeEncoder.encode(key));
            logger.info("delete key:" + key);

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * Remove the specified keys.
     *
     * @param key
     *
     * @return false if redis did not execute the option
     */
    public boolean expire(String key, int seconds) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.expire(SafeEncoder.encode(key), seconds);
            logger.info("expire key:" + key + " time after " + seconds + " seconds.");

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * Delete all the keys of all the existing databases, not just the currently selected one.
     *
     * @return false if redis did not execute the option
     */
    public boolean flushall() {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            result = jedis.flushAll();
            logger.info("redis client name: " + this.getCacheName() + " flushall.");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return "OK".equalsIgnoreCase(result);
    }

    public void shutdown() {
        try {
            this.jedisPool.destroy();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Get the bytes representing the given serialized object.
     */
    protected byte[] serialize(Object o) {
        if (o == null) {
            // throw new NullPointerException("Can't serialize null");
            return new byte[0];
        }
        byte[] rv = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(o);
            os.close();
            bos.close();
            rv = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
        return rv;
    }

    /**
     * Get the object represented by the given serialized bytes.
     */
    protected Object deserialize(byte[] in) {
        Object rv = null;
        try {
            if (in != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(in);
                ObjectInputStream is = new ObjectInputStream(bis);
                rv = is.readObject();
                is.close();
                bis.close();
            }
        } catch (IOException e) {
            logger.warn("Caught IOException decoding %d bytes of data", e);
        } catch (ClassNotFoundException e) {
            logger.warn("Caught CNFE decoding %d bytes of data", e);
        }
        return rv;
    }

    public void hput(String key, String field, Serializable fieldValue) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.hset(SafeEncoder.encode(key), SafeEncoder.encode(field), serialize(fieldValue));
            logger.info("hset key:" + key + " field:" + field);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Object hget(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            byte[] value = jedis.hget(SafeEncoder.encode(key), SafeEncoder.encode(field));
            logger.info("hget key:" + key + " field:" + field);

            return deserialize(value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public boolean hdel(String key, String field) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            long value = jedis.hdel(SafeEncoder.encode(key), SafeEncoder.encode(field));
            logger.info("hget key:" + key + ", field:" + field);

            return value == 1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<String> hKeys(String key) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Set<byte[]> hkeys = jedis.hkeys(SafeEncoder.encode(key));
            logger.info("hkeys key:" + key);
            if (CollectionUtils.isEmpty(hkeys)) {
                return new HashSet<String>(1);
            } else {
                Set<String> keys = new HashSet<String>(hkeys.size());
                for (byte[] bb : hkeys) {
                    keys.add(SafeEncoder.encode(bb));
                }
                return keys;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<Object> hValues(String key) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            List<byte[]> hvals = jedis.hvals(SafeEncoder.encode(key));
            logger.info("hvals key:" + key);
            if (CollectionUtils.isEmpty(hvals)) {
                return new ArrayList<Object>(1);
            } else {
                List<Object> ret = new ArrayList<Object>(hvals.size());
                for (byte[] bb : hvals) {
                    ret.add(deserialize(bb));
                }
                return ret;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean hExists(String key, String field) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            boolean ret = jedis.hexists(SafeEncoder.encode(key), SafeEncoder.encode(field));
            logger.info("hexists key:" + key + ", field:" + field);

            return ret;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long hLen(String key) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            long ret = jedis.hlen(SafeEncoder.encode(key));
            logger.info("hlen key:" + key);

            return ret;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private Map<String, Object> decodeMap(final Map<byte[], byte[]> values) {
        if (MapUtils.isEmpty(values)) {
            return Collections.emptyMap();
        }
        Map<byte[], byte[]> copy = new HashMap<byte[], byte[]>(values);
        Iterator<Entry<byte[], byte[]>> iterator = copy.entrySet().iterator();
        Map<String, Object> ret = new HashMap<String, Object>();
        while (iterator.hasNext()) {
            Entry<byte[], byte[]> next = iterator.next();
            ret.put(SafeEncoder.encode(next.getKey()), deserialize(next.getValue()));
        }

        return ret;
    }

    public Map<String, Object> hGetAll(String key) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Map<byte[], byte[]> hgetAll = jedis.hgetAll(SafeEncoder.encode(key));
            logger.info("hgetAll key:" + key);

            return decodeMap(hgetAll);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private Map<byte[], byte[]> encodeMap(final Map<String, Serializable> values) {
        if (MapUtils.isEmpty(values)) {
            return Collections.emptyMap();
        }
        Map<String, Serializable> copy = new HashMap<String, Serializable>(values);
        Iterator<Entry<String, Serializable>> iterator = copy.entrySet().iterator();
        Map<byte[], byte[]> ret = new HashMap<byte[], byte[]>();
        while (iterator.hasNext()) {
            Entry<String, Serializable> next = iterator.next();
            ret.put(SafeEncoder.encode(next.getKey()), serialize(next.getValue()));
        }

        return ret;
    }

    public void hmSet(String key, Map<String, Serializable> values) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.hmset(SafeEncoder.encode(key), encodeMap(values));
            logger.info("hmSet key:" + key + ", field:" + values.keySet());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private byte[][] encodeArray(final String[] array) {
        if (ArrayUtils.isEmpty(array)) {
            return new byte[0][0];
        }
        int len = array.length;
        List<byte[]> list = new ArrayList<byte[]>(len);
        for (int i = 0; i < len; i++) {
            list.add(SafeEncoder.encode(array[i]));
        }
        return list.toArray(new byte[len][0]);
    }

    public List<Object> hmGet(String key, String... fields) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            List<byte[]> hmget = jedis.hmget(SafeEncoder.encode(key), encodeArray(fields));
            logger.info("hmGet key:" + key + ", fields:" + Arrays.toString(fields));
            if (CollectionUtils.isEmpty(hmget)) {
                return new ArrayList<Object>(1);
            } else {
                List<Object> ret = new ArrayList<Object>(hmget.size());
                for (byte[] bb : hmget) {
                    ret.add(deserialize(bb));
                }
                return ret;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> hmGetByStringSerializer(String key, String... fields) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            List<String> hmget = jedis.hmget(key, fields);
            logger.info("hmGet key:" + key + ", fields:" + Arrays.toString(fields));
            if (CollectionUtils.isEmpty(hmget)) {
                return new ArrayList<String>(1);
            } else {
                return hmget;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void hmSetByStringSerializer(String key, Map<String, String> values) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            if (MapUtils.isEmpty(values)) {
                values = Collections.emptyMap();
            }
            jedis.hmset(key, values);
            // LOG.info("hmSet key:" + key + " field:" + values.keySet());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean sAdd(String key, String member) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Long r = jedis.sadd(key, member);
            return r == 1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean sRem(String key, String member) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Long r = jedis.srem(key, member);
            return r == 1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<String> sMembers(String key) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Set<String> out = jedis.smembers(key);

            return out;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Boolean sIsMember(String key, String member) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Boolean out = jedis.sismember(key, member);
            return out;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long sInterStore(String dstkey, String... keys) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Long out = jedis.sinterstore(dstkey, keys);
            return out;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean lpush(String key, Object value) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();

            // long begin = System.currentTimeMillis();
            jedis.lpush(SafeEncoder.encode(key), jsonSerialize(value));
            // long end = System.currentTimeMillis();
            // LOG.info("lpush key:" + key + " spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return true;
    }

    public Object lpop(String key, Class<?> cls) throws Exception {
        byte[] data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.lpop(SafeEncoder.encode(key));
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return this.jsonDeserialize(data, cls);
    }

    public boolean rpush(String key, Object value) throws Exception {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();

            // long begin = System.currentTimeMillis();
            jedis.rpush(SafeEncoder.encode(key), jsonSerialize(value));
            // long end = System.currentTimeMillis();
            // LOG.info("rpush key:" + key + " spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return true;
    }

    public Object rpop(String key, Class<?> cls) throws Exception {
        byte[] data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.rpop(SafeEncoder.encode(key));
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return this.jsonDeserialize(data, cls);
    }

    public boolean ltrim(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            jedis.ltrim(SafeEncoder.encode(key), start, end);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return true;
    }

    public List<Object> lrange(String key, int start, int end, Class<?> cls) {
        List<byte[]> data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.lrange(SafeEncoder.encode(key), start, end);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        List<Object> result = new ArrayList<>();
        data.forEach(d -> {
            result.add(this.jsonDeserialize(d, cls));
        });
        return result;
    }

    public long lrem(String key, int count, String value) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            long ret = jedis.lrem(key, count, value);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
            return ret;
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long llen(String key) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            long ret = jedis.llen(SafeEncoder.encode(key));
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
            return ret;
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public Long incr(String key) throws Exception {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            data = jedis.incr(SafeEncoder.encode(key));
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return data;
    }

    public Long decr(String key) throws Exception {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            data = jedis.decr(SafeEncoder.encode(key));
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return data;
    }

    public Long incrBy(final String key, final long integer) throws Exception {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            data = jedis.incrBy(key, integer);
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return data;
    }
    public Long zadd(String key, double score, String member) {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            data = jedis.zadd(key, score, member);
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }
    public Long zcard(String key) {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            data = jedis.zcard(key);
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }
    public Long zrem(String key, String... members) {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.zrem(key, members);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }
    public Long zcard(String key, double min, double max) {
        Long data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.zcount(key, min, max);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }
    public Set<String> zrevrange(String key, long start, long end) {
        Set<String> data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.zrevrange(key, start, end);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }
    public Set<String> zrange(String key, long start, long end) {
        Set<String> data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            // long begin = System.currentTimeMillis();
            data = jedis.zrange(key, start, end);
            // long end = System.currentTimeMillis();
            // LOG.info("getValueFromCache spends: " + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return data;
    }

    /**
     * Set key to hold string value if key does not exist. In that case, it is equal to SET.
     * When key already holds a value, no operation is performed. SETNX is short for "SET if N ot e X ists".
     *
     * @param key        Key to be operated.
     * @param value      Value to be set.
     * @param expiration Expiration time
     *
     * @return 1 if the key was set, 0 if hte key was not set.
     *
     * @throws Exception if execute failed.
     * @see <a href="http://redis.io/commands/setnx">Redis: SETNX</a>
     */
    public Long setnx(String key, Object value, int expiration) throws Exception {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            result = jedis.setnx(SafeEncoder.encode(key), serialize(value));
            jedis.expire(SafeEncoder.encode(key), expiration);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the bytes representing the given serialized object.
     */
    protected byte[] jsonSerialize(Object o) {
        return JSON.toJSONBytes(o);
    }

    /**
     * Get the object represented by the given serialized bytes.
     */
    protected Object jsonDeserialize(byte[] in, Class<?> cls) {
        if (in == null || in.length == 0) {
            return null;
        }
        Object res = JSON.parseObject(in, cls);
        return res;
    }

    public void destroy() throws Exception {
        this.jedisPool.destroy();

    }

    public String getRedisServerHost() {
        return redisServerHost;
    }

    public void setRedisServerHost(String redisServerHost) {
        this.redisServerHost = redisServerHost;
    }

    public String getRedisAuthKey() {
        return redisAuthKey;
    }

    public void setRedisAuthKey(String redisAuthKey) {
        this.redisAuthKey = redisAuthKey;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public int getRedisServerPort() {
        return redisServerPort;
    }

    public void setRedisServerPort(int redisServerPort) {
        this.redisServerPort = redisServerPort;
    }

    /**
     * @return the testOnBorrow
     */
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    /**
     * @param testOnBorrow the testOnBorrow to set
     */
    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    /**
     * @return the minIdle
     */
    public int getMinIdle() {
        return minIdle;
    }

    /**
     * @param minIdle the minIdle to set
     */
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    /**
     * @return the testOnReturn
     */
    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    /**
     * @param testOnReturn the testOnReturn to set
     */
    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public String getLiteralRedisServer() {
        return redisServerHost + ":" + redisServerPort;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public int getTimeout() {
        return timeout;
    }


    public String setString(String key, String value, Integer expiration) throws Exception {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();

            long begin = System.currentTimeMillis();
            if (expiration > 0) {
                result = jedis.setex(key, expiration, value);
            } else {
                result = jedis.set(key, value);
            }
            long end = System.currentTimeMillis();
            logger.info("set key:" + key + ", spends: " + (end - begin) + "ms");
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public String getString(String key) throws Exception {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (jedis != null) {
                jedis.close();
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }
}
