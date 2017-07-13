package com.neuandroid.departify;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.deeparteffects.sdk.android.DeepArtEffectsClient;
import com.deeparteffects.sdk.android.model.Result;
import com.deeparteffects.sdk.android.model.Styles;
import com.deeparteffects.sdk.android.model.UploadRequest;
import com.deeparteffects.sdk.android.model.UploadResponse;

import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String apiKey = "", accessKey = "", secretKey = "";

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

    private static final int CHECK_RESULT_INTERVAL_IN_MS = 2500;
    private static final int IMAGE_MAX_SIDE_LENGTH = 768;

    private static final int MSG_REQUEST_TIMEOUT = 100;
    private static final int MSG_REQUEST_SUCCEED = 101;

    @BindView(R.id.rv_art_styles)
    RecyclerView rvArtStyles;
    @BindView(R.id.iv_departified_pic)
    ImageView ivDepartifiedPic;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    private GridLayoutManager mLayoutManager;
    private DeepArtEffectsClient deepArtEffectsClient;
    private Bitmap mImageBitmap;
    private Context mContext;
    private ArtStyleAdapter styleAdapter;

    private boolean isProcessing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;

        rvArtStyles.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
//        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return styleAdapter.getItemViewType(position);
//            }
//        });
        rvArtStyles.setLayoutManager(mLayoutManager);
        rvArtStyles.setItemAnimator(new DefaultItemAnimator());


        String packageName = mContext.getApplicationContext().getPackageName();
        try {
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                apiKey = appInfo.metaData.getString("DEEP_ART_EFFECT_API_KEY");
                accessKey = appInfo.metaData.getString("DEEP_ART_EFFECT_ACCESS_KEY");
                secretKey = appInfo.metaData.getString("DEEP_ART_EFFECT_SECRET_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ApiClientFactory factory = new ApiClientFactory()
                .apiKey(apiKey) //TODO
                .region("eu-west-1")
                .credentialsProvider(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new BasicAWSCredentials(accessKey, secretKey); // TODO
                    }

                    @Override
                    public void refresh() {
                    }
                });
        deepArtEffectsClient = factory.build(DeepArtEffectsClient.class);

        loadingStyles();
    }

    private void loadingStyles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Styles styles = deepArtEffectsClient.stylesGet();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        styleAdapter = new ArtStyleAdapter(
                                getApplicationContext(),
                                styles,
                                new ArtStyleAdapter.IClickListener() {
                                    @Override
                                    public void onClick(String styleId) {
                                        if (!isProcessing) {
                                            if (mImageBitmap != null) {
                                                Log.d(TAG, String.format("Style with ID %s clicked.", styleId));
                                                isProcessing = true;
                                                pbLoading.setVisibility(View.VISIBLE);
                                                uploadImage(styleId);
                                            } else {
                                                Toast.makeText(mContext, "Please choose a picture first",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                        );
                        rvArtStyles.setAdapter(styleAdapter);
//                        styleAdapter.updateStyles(styles);
                        pbLoading.setVisibility(View.GONE);
//                        mStatusText.setText("");
                    }
                });
            }
        }).start();
    }

    private void uploadImage(final String styleId) {
//        mStatusText.setText("Uploading picture...");
        Log.d(TAG, String.format("Upload image with style id %s", styleId));
        final MyHandler myHandler = new MyHandler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                UploadRequest uploadRequest = new UploadRequest();
                uploadRequest.setStyleId(styleId);
                uploadRequest.setImageBase64Encoded(convertBitmapToBase64(mImageBitmap));
                UploadResponse response = null;
                try {
                     response = deepArtEffectsClient.uploadPost(uploadRequest);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                    myHandler.sendEmptyMessage(MSG_REQUEST_TIMEOUT);
                }
                if (response == null) {
                    return;
                }
                String submissionId = response.getSubmissionId();
                Log.d(TAG, String.format("Upload complete. Got submissionId %s", response.getSubmissionId()));
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new ImageReadyCheckTimer(submissionId),
                        CHECK_RESULT_INTERVAL_IN_MS, CHECK_RESULT_INTERVAL_IN_MS);
                myHandler.sendEmptyMessage(MSG_REQUEST_SUCCEED);
            }
        }).start();
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, 0);
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
                break;
            case R.id.action_camera:
                break;
            case R.id.action_gallery:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_GALLERY);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                break;
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    mImageBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),
                            this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
                    ivDepartifiedPic.setImageBitmap(mImageBitmap);
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ImageReadyCheckTimer extends TimerTask {
        private String mSubmissionId;

        public ImageReadyCheckTimer(String submissionId) {
            mSubmissionId = String.valueOf(submissionId);
        }

        @Override
        public void run() {
            try {
                final Result result = deepArtEffectsClient.resultGet(mSubmissionId);
                String submissionStatus = result.getStatus();
                Log.d(TAG, String.format("Submission status is %s", submissionStatus));
                if (submissionStatus.equals(SubmissionStatus.FINISHED)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext).load(result.getUrl()).listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    pbLoading.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    pbLoading.setVisibility(View.GONE);
                                    return false;
                                }
                            }).centerCrop().crossFade().into(ivDepartifiedPic);
                            ivDepartifiedPic.setVisibility(View.VISIBLE);
//                            mStatusText.setText("");
                        }
                    });
                    isProcessing = false;
                    cancel();
                }
            } catch (Exception e) {
                cancel();
            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REQUEST_SUCCEED:
                    break;
                case MSG_REQUEST_TIMEOUT:
                    pbLoading.setVisibility(View.GONE);
                    isProcessing = false;
                    Toast.makeText(mContext, "Request failed, time out", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
