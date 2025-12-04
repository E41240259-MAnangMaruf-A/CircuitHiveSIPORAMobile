package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sipora.R;

public class HomeActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView loadingText, openingTitle, openingSubtitle;
    private ImageView openingLogo;
    private int progressStatus = 0;
    private Handler progressHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.progress_bar);
        loadingText = findViewById(R.id.loading_text);
        openingLogo = findViewById(R.id.opening_logo);
        openingTitle = findViewById(R.id.opening_title);
        openingSubtitle = findViewById(R.id.opening_subtitle);
        startEntranceAnimations();
    }

    private void startEntranceAnimations() {
        AnimationSet logoAnimation = new AnimationSet(true);
        AlphaAnimation logoFadeIn = new AlphaAnimation(0, 1);
        logoFadeIn.setDuration(800);
        ScaleAnimation logoScale = new ScaleAnimation(0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        logoScale.setDuration(800);
        logoAnimation.addAnimation(logoFadeIn);
        logoAnimation.addAnimation(logoScale);
        logoAnimation.setInterpolator(new DecelerateInterpolator());
        openingLogo.startAnimation(logoAnimation);
        openingLogo.setAlpha(1f);

        new Handler().postDelayed(() -> {
            AnimationSet titleAnimation = new AnimationSet(true);
            AlphaAnimation titleFadeIn = new AlphaAnimation(0, 1);
            titleFadeIn.setDuration(600);
            TranslateAnimation titleSlideUp = new TranslateAnimation(0, 0, 30, 0);
            titleSlideUp.setDuration(600);
            titleAnimation.addAnimation(titleFadeIn);
            titleAnimation.addAnimation(titleSlideUp);
            titleAnimation.setInterpolator(new DecelerateInterpolator());
            openingTitle.startAnimation(titleAnimation);
            openingTitle.setAlpha(1f);
            openingTitle.setTranslationY(0);
        }, 300);

        new Handler().postDelayed(() -> {
            AnimationSet subtitleAnimation = new AnimationSet(true);
            AlphaAnimation subtitleFadeIn = new AlphaAnimation(0, 1);
            subtitleFadeIn.setDuration(600);
            TranslateAnimation subtitleSlideUp = new TranslateAnimation(0, 0, 20, 0);
            subtitleSlideUp.setDuration(600);
            subtitleAnimation.addAnimation(subtitleFadeIn);
            subtitleAnimation.addAnimation(subtitleSlideUp);
            subtitleAnimation.setInterpolator(new DecelerateInterpolator());
            openingSubtitle.startAnimation(subtitleAnimation);
            openingSubtitle.setAlpha(1f);
            openingSubtitle.setTranslationY(0);
        }, 500);

        new Handler().postDelayed(() -> {
            AlphaAnimation progressFadeIn = new AlphaAnimation(0, 1);
            progressFadeIn.setDuration(400);
            progressBar.startAnimation(progressFadeIn);
            progressBar.setAlpha(1f);

            AlphaAnimation textFadeIn = new AlphaAnimation(0, 1);
            textFadeIn.setDuration(400);
            loadingText.startAnimation(textFadeIn);
            loadingText.setAlpha(1f);

            startProgressAnimation();
        }, 800);
    }

    private void startProgressAnimation() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        progressStatus = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;

                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);

                            if (loadingText != null) {
                                loadingText.setText("Loading " + progressStatus + "%");
                                if (progressStatus % 10 == 0) {
                                    ScaleAnimation pulse = new ScaleAnimation(1f, 1.1f, 1f, 1.1f,
                                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    pulse.setDuration(200);
                                    pulse.setRepeatCount(1);
                                    pulse.setRepeatMode(Animation.REVERSE);
                                    loadingText.startAnimation(pulse);
                                }
                            }
                        }
                    });

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startExitAnimations();
                    }
                });
            }
        }).start();
    }

    private void startExitAnimations() {
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(400);
        fadeOut.setFillAfter(true);

        openingLogo.startAnimation(fadeOut);
        openingTitle.startAnimation(fadeOut);
        openingSubtitle.startAnimation(fadeOut);
        progressBar.startAnimation(fadeOut);
        loadingText.startAnimation(fadeOut);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(HomeActivity.this, OpeningActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 400);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacksAndMessages(null);
    }
}