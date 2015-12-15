import org.json.JSONException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luke on 11/24/15.
 */
public class RedisConnect {

    public RedisConnect() {
    }
    public void InsertToRedis(String key ,Map<String, String> map, String dbUrl) throws JSONException{
        Jedis jedis = new Jedis(dbUrl);
        jedis.hmset(key, map);
        jedis.close();
    }
    public void InsertToRedis(String key ,String domainName, Map<String, String> map, String dbUrl) throws JSONException{
        Jedis jedis = new Jedis(dbUrl);
        jedis.hmset(key, map);
        jedis.hmset(domainName, map);
        jedis.close();
    }
    public Map<String, String> QueryRedis(String name, String dbUrl) throws JSONException{
        Map<String, String> map = new HashMap<String, String>();
        Jedis jedis = new Jedis(dbUrl);
        map =  jedis.hgetAll(name);
        jedis.close();
        return map;
    }
}

