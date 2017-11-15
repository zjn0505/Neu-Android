package xyz.jienan.pushpull.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import xyz.jienan.pushpull.R;

public class MainActivity extends AppCompatActivity {

    private OnBackPressedListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mListener != null && mListener.onBackPressed()) {

        } else{
            super.onBackPressed();
        }

    }

    public void setBackPressListener(OnBackPressedListener listener) {
        mListener = listener;
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }
}