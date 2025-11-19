package com.example.sportconnection.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class SwipeCardHelper implements View.OnTouchListener {

    private static final int SWIPE_THRESHOLD = 300;
    private static final int ROTATION_DEGREES = 15;

    private float startX;
    private float startY;
    private float currentX;
    private float currentY;
    private boolean isDragging = false;

    private final View cardView;
    private final SwipeListener swipeListener;

    public interface SwipeListener {
        void onSwipeRight();
        void onSwipeLeft();
        void onCardExited();
    }

    public SwipeCardHelper(View cardView, SwipeListener swipeListener) {
        this.cardView = cardView;
        this.swipeListener = swipeListener;
        cardView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!isDragging) return false;

                currentX = event.getRawX();
                currentY = event.getRawY();

                float deltaX = currentX - startX;
                float deltaY = currentY - startY;

                // Mover la card
                cardView.setTranslationX(deltaX);
                cardView.setTranslationY(deltaY);

                // Rotar la card según la dirección
                float rotation = (deltaX / cardView.getWidth()) * ROTATION_DEGREES;
                cardView.setRotation(rotation);

                // Cambiar opacidad según la distancia
                float alpha = 1 - Math.abs(deltaX) / (cardView.getWidth() * 2);
                cardView.setAlpha(Math.max(alpha, 0.5f));

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;

                float finalDeltaX = currentX - startX;

                if (Math.abs(finalDeltaX) > SWIPE_THRESHOLD) {
                    // Swipe completado
                    if (finalDeltaX > 0) {
                        animateSwipeRight();
                    } else {
                        animateSwipeLeft();
                    }
                } else {
                    // Regresar a la posición original
                    animateReturn();
                }
                return true;
        }
        return false;
    }

    private void animateSwipeRight() {
        cardView.animate()
                .translationX(cardView.getWidth() * 2)
                .rotation(ROTATION_DEGREES * 2)
                .alpha(0)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListener.onSwipeRight();
                        removeCard();
                    }
                })
                .start();
    }

    private void animateSwipeLeft() {
        cardView.animate()
                .translationX(-cardView.getWidth() * 2)
                .rotation(-ROTATION_DEGREES * 2)
                .alpha(0)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListener.onSwipeLeft();
                        removeCard();
                    }
                })
                .start();
    }

    private void animateReturn() {
        cardView.animate()
                .translationX(0)
                .translationY(0)
                .rotation(0)
                .alpha(1)
                .setDuration(200)
                .setListener(null)
                .start();
    }

    private void removeCard() {
        cardView.post(() -> {
            if (cardView.getParent() != null) {
                ((ViewGroup) cardView.getParent()).removeView(cardView);
                swipeListener.onCardExited();
            }
        });
    }

    public void animateProgrammaticSwipeRight() {
        animateSwipeRight();
    }

    public void animateProgrammaticSwipeLeft() {
        animateSwipeLeft();
    }
}

