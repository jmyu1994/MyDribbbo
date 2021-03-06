package jmyu.ufl.edu.mydribbbo.dribbbo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jmyu.ufl.edu.mydribbbo.model.Bucket;
import jmyu.ufl.edu.mydribbbo.model.Shot;
import jmyu.ufl.edu.mydribbbo.model.User;
import jmyu.ufl.edu.mydribbbo.utils.ModelUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static jmyu.ufl.edu.mydribbbo.view.shot_detail.ShotFragment.KEY_SHOT;

/**
 * Created by jmyu on 7/4/18.
 */

public class Dribbbo {

    private static final String SP_AUTH = "auth";
    private static final String KEY_TOKEN = "token";

    private static final String API_URL = "https://api.dribbble.com/v2/";

    private static final String SHOTS_END_POINT = API_URL + "popular_shots";
    private static final String USER_END_POINT = API_URL + "user";
    private static final String KEY_USER = "user";
    private static final String BUCKETS_END_POINT = API_URL + "buckets";
    private static final String SP_BUCKET = "bucket data";
    private static final String KEY_BUCKET = "bucket";
    private static final String KEY_SHOT_BUCKET_MAP = "shot_bucket_map" ;
    private static final String KEY_BUCKET_SHOT_MAP = "bucket_shot_map";


    private static String token;
    private static User user;
    private static OkHttpClient client;
    private static List<Bucket> buckets;
    private static List<Shot> shots;
    private static Map<String, List<String>> shot_bucket_ids;
    private static Map<String, List<String>> bucket_shot_ids;

    private static Context context;
    private static List<String> userBucketIDs;


    public static void init(Context context) {
        Dribbbo.buckets = ModelUtils.read(context, KEY_BUCKET, new TypeToken<List<Bucket>>(){});
        System.out.println(buckets);
        if (buckets == null) {
            // when buckets size is zero, it will automatically generate fake buckets data.
            buckets = new ArrayList<>();
        }
        Dribbbo.shots = ModelUtils.read(context, KEY_SHOT, new TypeToken<List<Shot>>(){});
        if (shots == null) {
            // when buckets size is 0, it will automatically generate fake buckets data.
            shots = new ArrayList<>();
        }
        Dribbbo.shot_bucket_ids = ModelUtils.read(context, KEY_SHOT_BUCKET_MAP, new TypeToken<Map<String, List<String>>>(){});
        if (shot_bucket_ids == null) {
            shot_bucket_ids = new HashMap<>();
        }
        Dribbbo.bucket_shot_ids = ModelUtils.read(context, KEY_BUCKET_SHOT_MAP, new TypeToken<Map<String, List<String>>>(){});
        if (bucket_shot_ids == null) {
            bucket_shot_ids = new HashMap<>();
        }

        shot_bucket_ids = new HashMap<>();
        Dribbbo.context = context;
        client = new OkHttpClient();
        token = loadToken(context);
        if (token != null) {
            user = loadUser(context);
        }
    }

    private static User loadUser(Context context) {
        return ModelUtils.read(context, KEY_USER, new TypeToken<User>(){});
    }

    private static String loadToken(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_AUTH, Context.MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, null);
    }

    public static boolean isLoggedIn() {
//        return token != null;
        return false;
    }

    public static void login(Context context, String token) throws IOException {
        Dribbbo.token = token;
        storeToken(context, token);

        Dribbbo.user = getUser();
        storeUser(context, user);
        Log.d("Jimmy", user.name);
        Log.d("Jimmy", user.avatar_url);
    }

    private static void storeToken(Context context, String token) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_AUTH, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public static void storeUser(Context context, User user) {
        ModelUtils.save(context, KEY_USER, user);
    }
    public static List<Shot> getShots(int page) throws IOException {
        String url = SHOTS_END_POINT + "?page=" + page;
        Response response = makeGetRequest(url);
        Log.d("Jimmy", "fake shot data generated");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fakeShotData(page);
    }

    private static Response makeGetRequest(String url) throws IOException {
        Request request = authRequestBuilder(url).build();
        return makeRequest(request);
    }

    private static Response makeRequest(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    public static User getUser() throws IOException, JsonSyntaxException {
        Response response =  makeGetRequest(USER_END_POINT);
        return ModelUtils.toObject(response.body().string(), new TypeToken<User>(){});
    }

    private static Request.Builder authRequestBuilder(String url) {
        return new Request.Builder().addHeader("Authorization", "Bearer " + token)
                .url(url);
    }

    private static List<Shot> fakeShotData(int page) {
        List<Shot> shotList = new ArrayList<>();
        if (shots.size() <= 30) {
            Random random = new Random();
            for (int i = 0; i < (page < 3 ? 12 : 6); ++i) {
                Shot shot = new Shot();
                shot.id = "shot#" + String.valueOf(random.nextInt(10000));
                shot.title = "shot" + String.valueOf(i + (page - 1) * 12);
                shot.views_count = random.nextInt(10000);
                shot.likes_count = random.nextInt(200);
                shot.buckets_count = random.nextInt(50);
                shot.description = shot.title + " description";

                shot.images = new HashMap<>();
                shot.images.put(Shot.IMAGE_HIDPI, imageUrls[random.nextInt(imageUrls.length)]);

                shot.user = new User();
                shot.user.name = shot.title + " author";

                shotList.add(shot);
                shot_bucket_ids.put(shot.id, new ArrayList<>());
            }
            shots.addAll(shotList);
            ModelUtils.save(context, KEY_SHOT, shots);
            ModelUtils.save(context, KEY_SHOT_BUCKET_MAP, shot_bucket_ids);
        } else {
            for (int i = 0; i < (page < 3 ? 12 : 6); ++i) {
                int index = (page - 1) * 12 + i;
                shotList.add(shots.get(index));
            }
        }
        return shotList;
    }

    private static final String[] imageUrls = {
            "http://35.196.58.1/yelp-business-photos/-EgKmv2dcfZPP9sKPrpFRQ.jpg",
            "http://35.196.58.1/yelp-business-photos/kFARd0Ci3ZUlMq3wnOh7pA.jpg",
            "http://35.196.58.1/yelp-business-photos/_ZYTeDR44RCd5TfT-0iBcQ.jpg",
            "http://35.196.58.1/yelp-business-photos/kfARhz31OuZTDPfrCI2eLg.jpg",
            "http://35.196.58.1/yelp-business-photos/Z-Yz_a8fAP3qImq2dKVUqg.jpg",
            "http://35.196.58.1/yelp-business-photos/_kFSMrEdKS6XEVtubBchJg.jpg",
            "http://35.196.58.1/yelp-business-photos/ZZdtWxptjYN1cFxPd6a5bw.jpg",
            "http://35.196.58.1/yelp-business-photos/KFwfNcfVAk25eqIhC6C6Bg.jpg",
            "http://35.196.58.1/yelp-business-photos/Z_zhnfy-mdFYqzlmKI7Wng.jpg",
            "http://35.196.58.1/yelp-business-photos/kH6couimx13HIgYNgq9F2g.jpg",
            "http://35.196.58.1/yelp-business-photos/zZjQ22iQlIAQ7C2xClvaOw.jpg"
    };

    public static void logout(Context context) {
        storeToken(context, null);
        token = null;
        storeUser(context, null);
        user = null;
    }

    public static User getCurrentUser() {
        return user;
    }

    public static List<Bucket> getBuckets(int page) throws IOException {
        String url = BUCKETS_END_POINT + "?page=" + page;
        Response response = makeGetRequest(url);
        Log.d("Jimmy", "fake bucket data generated");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fakeBucketData(page);
    }

    private static List<Bucket> fakeBucketData(int page) {
        List<Bucket> bucketList = new ArrayList<>();
        if (buckets.size() <= 30) {
            Random random = new Random();
            for (int i = 0; i < (page < 3 ? 12 : 6); ++i) {
                Bucket bucket = new Bucket();
                bucket.id = "bucket#" + String.valueOf(random.nextInt(10000));
                bucket.name = "Bucket" + String.valueOf(i + (page - 1) * 12);
                bucket.shots_count = random.nextInt(10);
                bucketList.add(bucket);
                bucket.isChosen = false;
                bucket_shot_ids.put(bucket.id, new ArrayList<String>());
            }

            buckets.addAll(bucketList);
            ModelUtils.save(context, KEY_BUCKET, buckets);
            ModelUtils.save(context, KEY_BUCKET_SHOT_MAP, bucket_shot_ids);
        } else {
            for (int i = 0; i < (page < 3 ? 12 : 6); ++i) {
                int index = (page - 1) * 12 + i;
                bucketList.add(buckets.get(index));
            }
        }
        return bucketList;
    }

    public static Bucket newBucket(String bucketName, String bucketDescription) {
        Bucket bucket = new Bucket();
        bucket.name = bucketName;
        bucketDescription = bucketDescription;
        buckets.add(0, bucket);
        ModelUtils.save(context, KEY_BUCKET, buckets);
        return bucket;
    }

    public static List<String> getUserBucketIDs() {
        if (userBucketIDs == null) {
            List<String> userBucketIDs = new ArrayList<>();
            for (Bucket bucket : buckets) {
                userBucketIDs.add(bucket.id);
            }
        }
        return userBucketIDs;
    }

    public static void addBucketShot(String addedId, String id) {
    }

    public static void removeBucketShot(String removedId, String id) {
    }
}
