package com.neuandroid.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindViews({R.id.box_1,R.id.box_2,R.id.box_3,R.id.box_4,R.id.box_5,R.id.box_6,R.id.box_7,R.id.box_8,R.id.box_9})
    List<Button> btnViews;
    @BindView(R.id.tv_hint)
    TextView tvHint;
    @BindView(R.id.container)
    LinearLayout container;

    private Boolean playerOneTurn = true;
    private Boolean inGame = true;
    private ArrayList<Integer> playerOneStatus = new ArrayList<>();
    private ArrayList<Integer> playerTwoStatus = new ArrayList<>();

    private static Drawable drawable;

    private final static List<List<Integer>> WIN_STATE =
            Arrays.asList(
                    Arrays.asList(1,2,3),
                    Arrays.asList(4,5,6),
                    Arrays.asList(7,8,9),
                    Arrays.asList(1,4,7),
                    Arrays.asList(2,5,8),
                    Arrays.asList(3,6,9),
                    Arrays.asList(1,5,9),
                    Arrays.asList(3,5,7));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        reset();
    }

    /**
     * Set all the buttons to the initial status
     */
    static final ButterKnife.Action<View> INIT = new ButterKnife.Action<View>() {
        @Override
        public void apply(@NonNull View view, int index) {
            view.setTag(R.string.tag_index, index + 1);
            view.setTag(R.string.tag_taken, false);
            if (drawable == null)
                drawable = view.getBackground();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
        }
    };

    @OnClick({R.id.box_1,R.id.box_2,R.id.box_3,R.id.box_4,R.id.box_5,R.id.box_6,R.id.box_7,R.id.box_8,R.id.box_9})
    public void clickBox(Button btnBox) {
         if (inGame) {
             if ((boolean) btnBox.getTag(R.string.tag_taken)) {
                 return;
             }
             int index = (int) btnBox.getTag(R.string.tag_index);
             if (playerOneTurn) {
                 btnBox.setBackgroundColor(getResources().getColor(R.color.player_one));
                 addIndexToPlayerStatus(playerOneStatus, index);
             } else {
                 btnBox.setBackgroundColor(getResources().getColor(R.color.player_two));
                 addIndexToPlayerStatus(playerTwoStatus, index);
             }
             btnBox.setTag(R.string.tag_taken, true);
             checkResult();
             playerOneTurn = !playerOneTurn;
             Log.d("Player_status_p1", playerOneStatus.toString());
             Log.d("Player_status_p2", playerTwoStatus.toString());
         }
    }

    private void addIndexToPlayerStatus(ArrayList<Integer> playerStatus, int index) {
        playerStatus.add(index);
        Collections.sort(playerStatus);
    }

    private void checkResult() {
        if (playerOneStatus.size() < 3) {
            return;
        }
        for (List<Integer> win_temp : WIN_STATE) {
            if (playerOneStatus.containsAll(win_temp)) {
                tvHint.setText(getString(R.string.player_one_win));
                inGame = false;
                return;
            } else if (playerTwoStatus.containsAll(win_temp)) {
                tvHint.setText(getString(R.string.player_two_win));
                inGame = false;
                return;
            }
        }
        if (playerOneStatus.size() == 5) {
            tvHint.setText(getString(R.string.draw));
            inGame = false;
        }
    }

    private void reset() {
        ButterKnife.apply(btnViews, INIT);
    }

    @OnClick(R.id.tv_hint)
    public void resetBoard() {
        reset();
        inGame = true;
        playerOneTurn = true;
        playerOneStatus =  new ArrayList<Integer>();
        playerTwoStatus = new ArrayList<Integer>();
        tvHint.setText(getString(R.string.hint_default));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
//                View rootView = getWindow().getDecorView().getRootView(); // this is screenshot
                View rootView = container;
                if (rootView != null) {
                    Bitmap screenshot = screenShot(rootView);
                    showImage(screenshot);
                }
        }
        return true;
    }

    private Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void showImage(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.setView(imageView)
                .setTitle(R.string.btn_share).setPositiveButton(R.string.btn_share, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //
            }
        }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //
            }
        });

        builder.show();
    }

}
