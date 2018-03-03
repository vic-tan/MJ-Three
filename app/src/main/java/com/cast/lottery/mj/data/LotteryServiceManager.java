package com.cast.lottery.mj.data;

import android.os.Environment;
import android.util.Log;

import com.cast.lottery.mj.App;
import com.cast.lottery.mj.models.Lottery;
import com.cast.lottery.mj.models.LotteryDetail;
import com.cast.lottery.mj.utils.CacheUtils;
import com.cast.lottery.mj.utils.Constants;
import com.cast.lottery.mj.utils.LotteryUtils;
import com.cast.lottery.mj.utils.NetUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by feimeng0530 on 2016/3/17.
 */
public class LotteryServiceManager {
    public static volatile LotteryServiceManager instance;
    private OkHttpClient client;
    private LotteryService service;

    private Retrofit retrofit;
    private LotteryApiService apiService;

    private Retrofit apiRetrofit;
    private static String TAG = "LotteryServiceManager";


    public LotteryServiceManager() {
        init();
    }


    public static LotteryServiceManager getInstance() {
        if (instance == null) {
            synchronized (LotteryServiceManager.class) {
                if (instance == null) {
                    instance = new LotteryServiceManager();
                }
            }
        }
        return instance;
    }

    /**
     * initialize the OKHttpClient and retrofit
     */
    private void init() {
        client = new OkHttpClient.Builder()
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .readTimeout(3000, TimeUnit.MILLISECONDS)

                .cache(new Cache(CacheUtils.getDir(App.getAppContext()), CacheUtils.getCacheSize()))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if (!NetUtil.isNetAvailable()) {
                            request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                        }
                        Response response = chain.proceed(request);

                        if (NetUtil.isNetAvailable()) {
                            return response.newBuilder()
                                    .addHeader("Cache-Control", "max-age=0")
                                    .removeHeader("Pragma")
                                    .build();
                        } else {
                            return response.newBuilder()
                                    .addHeader("Cache-Control", "public, only-if-cached, max-stale=60*60*24*365")
                                    .removeHeader("pragma")
                                    .build();
                        }

                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.RETROFIT_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(LotteryService.class);
        apiRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.RETROFIT_API_VERSION_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiService = apiRetrofit.create(LotteryApiService.class);
    }

    @Deprecated
    public void getLastVersion(Subscriber subscriber, String token) {
        apiService.getLastVersion(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public Observable getLastVersion(String token) {
        return apiService.getLastVersion(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * The method is used to get the last data for 360 lottery
     *
     * @param subscriber
     */
    public void getLastData360(Subscriber subscriber) {

        service.geLastData360()
                .subscribeOn(Schedulers.io())
                .map(new Func1<Lottery, List>() {
                    @Override
                    public List call(Lottery lottery) {
                        return getLotteryList(lottery);
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    /**
     * @param lottery
     * @return
     */
    private List<Lottery.IEntity> getLotteryList(Lottery lottery) {
        Log.i(TAG, lottery.toString());
        List ret = new ArrayList();
        Map<String, String> available = LotteryUtils.getAllAvailable();
        Set keySet = available.keySet();
        List<Lottery.Entity> entity = lottery.getAllEntities();
        for (Lottery.Entity e : entity) {
            Log.i(TAG, "Lottery.Entity=" + (e == null ? "null" : e.getLotName()));
            if (e != null && keySet.contains(e.getLotName())) {
                ret.add(e);
            }
        }
        String s = new Gson().toJson(ret);
        File file = new File(Environment.getExternalStorageDirectory() +File.separator+ "lottery_local_cache");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("cache_file",file.getAbsolutePath());
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.write(s);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public Observable<LotteryDetail> getLotteryDetail(String lotId, String issue) {

        return service.getLotteryDetail(lotId, issue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get the detail for one issue of lottery
     *
     * @param subscriber
     * @param lotId
     * @param issue
     */
    public void getLotteryDetail(Subscriber subscriber, String lotId, String issue) {

        service.getLotteryDetail(lotId, issue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * @param subscriber
     * @param lotId
     */
    public void getHistory360(Subscriber subscriber, String lotId, String page) {

        service.geLotteryHistory(lotId, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public static void main(String[] args) {
        getInstance().getLastData360(new Subscriber<Lottery>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError" + e.getLocalizedMessage());
            }

            @Override
            public void onNext(Lottery o) {
                System.out.println(o.getValue220028());
            }
        });
    }

}
