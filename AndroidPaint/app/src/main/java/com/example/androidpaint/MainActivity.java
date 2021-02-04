package com.example.androidpaint;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class MainActivity extends AppCompatActivity {
    private DrawView game = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new DrawView(this);
        setContentView(game);
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_pause){
            game.pause();
            return true;
        }else if(id == R.id.action_restart){
            game = new DrawView(this);
            setContentView(game);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}