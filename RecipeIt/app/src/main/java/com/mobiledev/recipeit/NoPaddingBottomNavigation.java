package com.mobiledev.recipeit;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NoPaddingBottomNavigation extends BottomNavigationView {
    public NoPaddingBottomNavigation(@NonNull Context context) {
        super(context);
        init();
    }

    public NoPaddingBottomNavigation(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoPaddingBottomNavigation(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnApplyWindowInsetsListener(null);
        setPadding(0, 0, 0, 0);
    }
}
