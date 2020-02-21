package com.example.smartgym1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static com.example.smartgym1.FBref.refAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void disconnect(View view) {
        refAuth.signOut();
        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("stayConnect", false);
        editor.commit();
        Intent intent = new Intent(MainActivity.this, LoginOrRegisterActivity.class);
        startActivity(intent);
    }
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String str = item.getTitle().toString();
        Intent t;
        if(str.equals("profile")){
            t = new Intent(this,ProfileActivity.class);
            startActivity(t);
        }

        return super.onOptionsItemSelected(item);
    }
}


