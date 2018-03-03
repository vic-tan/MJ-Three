package com.cast.lottery.mj.data;

import android.content.Context;

import com.cast.lottery.mj.App;
import com.cast.lottery.mj.utils.CacheUtils;

import java.util.concurrent.TimeUnit;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetCallback;
import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kevin on 8/15/17.
 */

public class WebManager {

    private Retrofit retrofit;
    private WebService webService;

    private WebManager(){
        init();
    }

    private void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .readTimeout(3000, TimeUnit.MILLISECONDS)
                .cache(new Cache(CacheUtils.getDir(App.getAppContext()), CacheUtils.getCacheSize())).build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://appid.qq-app.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        webService = retrofit.create(WebService.class);
    }

    private static WebManager instance;
    public static WebManager getInstance() {
        if (instance == null) {
            synchronized (WebManager.class) {
                if (instance == null) {
                    instance = new WebManager();
                }
            }
        }
        return instance;
    }
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    public Subscription getWebUrlByPost(Subscriber subscriber,String body){
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE, body);
        return webService.getWebUrl(requestBody).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }


    public Subscription getWebUrlByGet(Subscriber subscriber,String appid) {
       return webService.getWebUrl(appid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    public void getAppInfo(Context ctx,String tableName,String objectId, GetCallback callback){
        BmobQuery bmobQuery = new BmobQuery(tableName);
        bmobQuery.getObject(ctx, objectId,callback);

    }
}
