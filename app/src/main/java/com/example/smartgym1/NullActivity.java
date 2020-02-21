package com.example.smartgym1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class NullActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_null);

    }

    @Override
    protected void onResume() {
        super.onResume();
    Intent t=new Intent(NullActivity.this,LoginOrRegisterActivity.class);
    startActivity(t);
    }
}
