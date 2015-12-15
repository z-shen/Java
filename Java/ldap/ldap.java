import org.json.JSONException;
import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.*;


/**
 * Created by luke on 10/30/15.
 */

public class Ldap {
   

    private String ldapUrl;
    private String account;
    private String password;
    private String base;
    private String dbUrl;

    public Ldap() {

    }
    public Ldap(String ldapUrl, String account, String password,String base,String dbUrl) {
        this.ldapUrl = ldapUrl;
        this.account = account;
        this.password = password;
        this.base = base;
        this.dbUrl = dbUrl;
    }
    public Map<String, JSONObject> query(ArrayList<String> userName) {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        RedisConnect redis = new RedisConnect();
        String[] attributeFilter = {"cn","sn","givenName","department","userPrincipalName","physicalDeliveryOfficeName","title","mail"};
        String sp = "com.sun.jndi.ldap.LdapCtxFactory";
        env.put(Context.INITIAL_CONTEXT_FACTORY, sp);
        env.put(Context.PROVIDER_URL, this.ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, this.account);
        env.put(Context.SECURITY_CREDENTIALS, this.password);

        NamingEnumeration<?> ldapResults = null;
        Map<String, JSONObject> result = new HashMap<String, JSONObject>();

        try {
            DirContext dctx = new InitialDirContext(env);
            SearchControls sc = new SearchControls();
            sc.setReturningAttributes(attributeFilter);
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

           

            for(int i=0; i<userName.size(); i++){

                Map<String, String> map = null;

                String queryString = userName.get(i);

                map = redis.QueryRedis(queryString, this.dbUrl);
                // GET Result from Redis
                if(!map.isEmpty()){
                    JSONObject obj = new JSONObject();
                    obj.put("Name", userName.get(i));
                    obj.put("FirstName", map.get("sn"));
                    obj.put("LastName", map.get("givenName"));
                    obj.put("Department", map.get("department"));
                    obj.put("Office", map.get("physicalDeliveryOfficeName"));
                    obj.put("DomainLocation",map.get("userPrincipalName").split("@")[1]);

                    result.put(userName.get(i).trim(),obj);
                }
                else{
                    // Query ldap if no result at redis
                    String filter = "CN="+queryString+"*";

                    ldapResults = dctx.search(this.base, filter, sc);
                    while (ldapResults.hasMoreElements()) {
                        SearchResult sr = (SearchResult) ldapResults.next();
                        Attributes attrs = sr.getAttributes();
                        Map<String, String> Usermap = new HashMap<String, String>();

                        //Check return value is not null
                        for(int j = 0; j<attributeFilter.length; j++) {
                            Usermap.put(attributeFilter[j], CheckValueEmpty(attrs, attributeFilter[j]));
                        }

                        JSONObject obj = new JSONObject();
                        obj.put("Name", userName.get(i));
                        obj.put("FirstName", Usermap.get("sn"));
                        obj.put("LastName",Usermap.get("givenName"));
                        obj.put("DomainLocation",Usermap.get("userPrincipalName").trim().split("@")[1]);
                        obj.put("Department", Usermap.get("department"));
                        obj.put("Office", Usermap.get("physicalDeliveryOfficeName"));

                        result.put(userName.get(i).trim(), obj);
                        //insert to redis
                        Thread t = new Thread(new LdapGenerator(queryString,Usermap,this.dbUrl));
                        t.start();
                    }
                }
            }
            dctx.close();
        }catch (NameNotFoundException e) {

        } catch (NamingException e) {
            log.error("Unable to connect to LDAP Server "+e);
        } catch(JSONException e) {
            log.error(e);
        }
        return result;
    }

    private String CheckValueEmpty(Attributes map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString().split(":")[1].trim();
    }
}

class LdapGenerator implements Runnable {
    Map<String, String> Usermap;
    String RedisdbUrl;
    String key;

    public LdapGenerator(String key,Map<String, String> Usermap,String dbUrl) {
        this.key = key;
        this.Usermap = Usermap;
        this.RedisdbUrl = dbUrl;
    }

    public void run() {
        RedisConnect redis = new RedisConnect();
        try {
           
            String domainName = Usermap.get("userPrincipalName");
            // check the account name and domain is same or not
            if (!(key.equals(domainName)))
                redis.InsertToRedis(key, domainName, Usermap, RedisdbUrl);
            else
                redis.InsertToRedis(key, Usermap, RedisdbUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
