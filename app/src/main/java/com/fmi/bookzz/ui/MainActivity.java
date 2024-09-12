package com.fmi.bookzz.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.databinding.ActivityMainBinding;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.IOnBackPressed;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.ui.user.MyProfileFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private ImageView profilePictureIV;
    private TextView usernameTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        setSupportActionBar(binding.appBarMain.toolbar);
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_books, R.id.nav_my_library, R.id.nav_chat, R.id.nav_friends, R.id.nav_goal, R.id.nav_schedule, R.id.nav_statistic, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        View headerView = navigationView.getHeaderView(0);
        profilePictureIV = headerView.findViewById(R.id.imageView);
        usernameTV = headerView.findViewById(R.id.usernameTV);
        if(RequestHelper.currentUser != null){
            if(RequestHelper.currentUser.getUsername() != null) {
                usernameTV.setText(RequestHelper.currentUser.getUsername());
            }
            if(RequestHelper.currentUser.getProfilePicture() != null){
                Bitmap profilePicture =  Helper.decodeBase64ToBitmap(RequestHelper.currentUser.getProfilePicture());
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profilePicture);
                roundedBitmapDrawable.setCircular(true);
                profilePictureIV.setImageDrawable(roundedBitmapDrawable);
                //profilePictureIV.setImageBitmap(profilePicture);
                profilePictureIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment profileFragment = new MyProfileFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.content_main, profileFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    Fragment parent = null;

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); delete this line
        // and start your fragment:

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }
}