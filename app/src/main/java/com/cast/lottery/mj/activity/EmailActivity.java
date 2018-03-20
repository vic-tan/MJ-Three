package com.cast.lottery.mj.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cast.lottery.mj.R;


/**
 * Created by Dmytro Denysenko on 5/4/15.
 */
public class EmailActivity extends AppCompatActivity {

    TextView title;
    EditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emial);
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.eixt_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EmailActivity.this, "提交成功", Toast.LENGTH_LONG).show();
                finish();
            }
        });


    }


}
