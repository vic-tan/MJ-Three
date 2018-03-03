package com.cast.lottery.mj.data;


import com.cast.lottery.mj.models.ApiVersion;


import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
/**
 * Created by feimeng0530 on 2016/3/17.
 */
public interface LotteryApiService {
    @GET("57051b47748aac1c1a000036")
    Observable<ApiVersion> getLastVersion(@Query("api_token") String api_token);
}
