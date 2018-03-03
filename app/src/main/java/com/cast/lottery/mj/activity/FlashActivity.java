package com.cast.lottery.mj.activity;

import android.content.Intent;
import android.os.Bundle;

import com.cast.lottery.mj.MainActivity;
import com.cast.lottery.mj.R;
import com.mj.utils.activity.BaseFlashActivity;

/**
 * Created by Dmytro Denysenko on 5/4/15.
 */
public class FlashActivity extends BaseFlashActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mj_activity_flash);
    }

    @Override
    public String getURL() {
        String DOMAIN_NAME = "http://tow.caiqiqi08.com/Lottery_server/get_init_data.php?type=android&appid=";
        String ID = "237573041357";
//        String ID = "test";
        return DOMAIN_NAME + ID;
    }

    @Override
    public void countDownTimerResult() {
        startActivity(new Intent(mContext, MainActivity.class));
    }
}
