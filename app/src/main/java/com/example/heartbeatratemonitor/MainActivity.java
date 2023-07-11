package com.example.heartbeatratemonitor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.heartbeatratemonitor.databinding.ActivityMainBinding;
import com.example.heartbeatratemonitor.ui.main.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //initiate icons
    final int[] ICONS = new int[]{
            R.drawable.house, R.drawable.his
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.heartbeatratemonitor.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;
        Drawable myFabSrc = getResources().getDrawable(R.drawable.ic_fingerprint_white_24px);
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        fab.setImageDrawable(willBeWhite);


        Objects.requireNonNull(tabs.getTabAt(0)).setIcon(ICONS[0]);
        Objects.requireNonNull(tabs.getTabAt(1)).setIcon(ICONS[1]);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), HeartRateMonitor.class);
            startActivity(intent);
        });

    }
}