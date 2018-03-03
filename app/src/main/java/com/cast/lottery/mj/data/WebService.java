package com.cast.lottery.mj.data;


import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by kevin on 8/15/17.
 */

public interface WebService {


    @POST("appgl/appShow/getInfo")
    Observable<Map> getWebUrl(@Body RequestBody body);

    @GET("frontApi/getAboutUs")
    Observable<Map> getWebUrl(@Query("appid") String appid);
}
