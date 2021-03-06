package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.adapters.HomeTabAdapter;
import com.applex.utsav.drawerActivities.AboutUs;
import com.applex.utsav.fragments.AllPujoFragment;
import com.applex.utsav.fragments.ClipsFragment;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.fragments.FeedsFragment;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.NotifCount;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.registration.LoginActivity;
import com.applex.utsav.utility.DialogUtils;
import com.applex.utsav.utility.MessagingService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import org.jsoup.Jsoup;
import java.util.Locale;
import java.util.Objects;

import static com.applex.utsav.fragments.ClipsFragment.mRecyclerView;
import static com.applex.utsav.utility.Constants.WHATSAPP_NUMBER;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @SuppressLint("StaticFieldLeak")
    static MainActivity instance;
    private DrawerLayout drawer;

    private IntroPref introPref;
    private ImageView toolbarImage;
    private String TYPE;
    private String GENDER;

    private String currentVersion;
    BroadcastReceiver myReceiver;

    NavigationView navigationView;
    ///NAV DRAWER VARIABLES/////
    ImageView displaypic, chandmala;
    TextView name, visits, upvoters;
    LinearLayout com_data;
    ///NAV DRAWER VARIABLES/////

    GoogleSignInClient mGooglesigninclient;

    ImageView notif, search, live;
    TextView notifDot;
    DocumentReference docref3;
    LottieAnimationView lottieAnimationView;

    private Boolean doubleBackPressed = false;
    public static ViewPager viewPager;
    public static int mode_changed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPref = new IntroPref(this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        /////////////////DAY OR NIGHT MODE///////////////////
        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            if(task.getResult().getBoolean("night_mode")) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_main);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGooglesigninclient = GoogleSignIn.getClient(this, googleSignInOptions);

        ///////////////NOTIFICATIONS///////////////////
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("MyNotifications","MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("users").addOnCompleteListener(task -> { });
        ///////////////NOTIFICATIONS///////////////////

        //////////////LATEST VERSION CHECK////////////////////
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new GetLatestVersion().execute();
        //////////////LATEST VERSION CHECK////////////////////

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        navigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple2)));

        name = hView.findViewById(R.id.nav_Name);
        displaypic = hView.findViewById(R.id.displaypic);
        com_data = hView.findViewById(R.id.com_data);
        visits = hView.findViewById(R.id.visits);
        upvoters = hView.findViewById(R.id.followers);
        chandmala = hView.findViewById(R.id.chandmala);
        notif = findViewById(R.id.notif);
        live = findViewById(R.id.live);
        notifDot = findViewById(R.id.notif_badge);
        search= findViewById(R.id.search);
        lottieAnimationView = findViewById(R.id.lottie);

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(MainActivity.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("listener") != null && value.getBoolean("listener")) {
                                FirebaseFirestore.getInstance().document("Mode/night_mode")
                                        .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().getBoolean("night_mode")) {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                        } else {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                        }
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                    new Handler().postDelayed(() -> {
                                        FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);
                                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        ///////////////Set Image Bitmap/////////////////////

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.decorative_item, options);
        int width = options.outWidth;
        if (width > displayWidth) {
            int widthRatio = Math.round((float) width / (float) displayWidth);
            options.inSampleSize = widthRatio;
        }
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.decorative_item, options);
        chandmala.setImageBitmap(scaledBitmap);
        ///////////////Set Image Bitmap/////////////////////

        hView.setOnClickListener(v -> {
            drawer.closeDrawers();
            new Handler().postDelayed(() -> {
//                if(TYPE.matches("com")){
//                    Intent i = new Intent(MainActivity.this, ActivityProfileCommittee.class);
//                    i.putExtra("uid", FirebaseAuth.getInstance().getUid());
//                    startActivity(i);
//                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                }
//                else if(TYPE.matches("indi")){
//                    Intent i = new Intent(MainActivity.this, ActivityProfileUser.class);
//                    i.putExtra("uid", FirebaseAuth.getInstance().getUid());
//                    startActivity(i);
//                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                }
                Intent i = new Intent(MainActivity.this, ActivityProfile.class);
                i.putExtra("uid", FirebaseAuth.getInstance().getUid());
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            },200);
        });

        toolbarImage= findViewById(R.id.toolbarimg1);
        toolbarImage.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        //NOTIFICATION//
        docref3= FirebaseFirestore.getInstance()
                .collection("Users/"+ FirebaseAuth.getInstance().getUid()+"/notifCount/")
                .document("notifCount");

        if(MessagingService.nCount == null) {
            notifDot.setVisibility(View.GONE);
        }
        else
            notifDot.setVisibility(View.VISIBLE);

        notifDot.setText(MessagingService.nCount);

        notif.setOnClickListener(view -> {
            NotifCount notifCount= new NotifCount();
            notifCount.setNotifCount(0);
            docref3.set(notifCount).addOnCompleteListener(task -> Log.d("Check", "nCount set to 0"));
            notifDot.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, ActivityNotification.class));
        });

        search.setOnClickListener(view -> {
            Intent i= new Intent(MainActivity.this, SearchActivity.class);
            startActivity(i);
        });

        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MainActivity.this, LiveActivity.class);
                startActivity(i);
            }
        });

//        FirebaseFirestore.getInstance().document("Live/Stream").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()){
//                    if(task.getResult().getBoolean("bool")){
//                        live.setVisibility(View.VISIBLE);
//                        lottieAnimationView.setVisibility(View.VISIBLE);
//                    }
//                    else{
//                        live.setVisibility(View.GONE);
//                        lottieAnimationView.setVisibility(View.GONE);
//                    }
//
//                }
//                else
//                    BasicUtility.showToast(MainActivity.this,"Somehing went wrong");
//            }
//        });

        TabLayout tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        setupViewPager();
        tabs.setupWithViewPager(viewPager);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).setIcon(R.drawable.ic_home_selected));
        Objects.requireNonNull(tabs.getTabAt(1)).setIcon(R.drawable.ic_people_unselected);
        Objects.requireNonNull(tabs.getTabAt(2)).setIcon(R.drawable.ic_video_unselected);
        Objects.requireNonNull(tabs.getTabAt(3)).setIcon(R.drawable.ic_durga_unselected);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    tab.setIcon(R.drawable.ic_home_selected);
                } else if(tab.getPosition() == 1) {
                    tab.setIcon(R.drawable.ic_people_selected);
                } else if(tab.getPosition() == 2) {
                    tab.setIcon(R.drawable.ic_video_selected);
                } else if(tab.getPosition() == 3) {
                    tab.setIcon(R.drawable.ic_durga_selected);
                }
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    tab.setIcon(R.drawable.ic_home_unselected);
                } else if(tab.getPosition() == 1) {
                    tab.setIcon(R.drawable.ic_people_unselected);
                } else if(tab.getPosition() == 2) {
                    tab.setIcon(R.drawable.ic_video_unselected);
                } else if(tab.getPosition() == 3) {
                    tab.setIcon(R.drawable.ic_durga_unselected);
                }
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    tab.setIcon(R.drawable.ic_home_selected);
                } else if(tab.getPosition() == 1) {
                    tab.setIcon(R.drawable.ic_people_selected);
                } else if(tab.getPosition() == 2) {
                    tab.setIcon(R.drawable.ic_video_selected);
                } else if(tab.getPosition() == 3) {
                    tab.setIcon(R.drawable.ic_durga_selected);
                }
            }
        });
    }

    private void setupViewPager() {
        HomeTabAdapter tabAdapter = new HomeTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new CommitteeFragment(), "");
        tabAdapter.addFragment(new FeedsFragment(), "");
        tabAdapter.addFragment(new ClipsFragment(), "");
        tabAdapter.addFragment(new AllPujoFragment(), "");
        viewPager.setAdapter(tabAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                if(position != 2 && mRecyclerView != null) {
                    RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
                    int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
                    int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

                    if (firstVisiblePosition >= 0) {
                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                            final RecyclerView.ViewHolder holder1 = mRecyclerView.findViewHolderForAdapterPosition(i);
                            ClipsFragment.ProgrammingViewHolder cvh1 = (ClipsFragment.ProgrammingViewHolder) holder1;
                            Objects.requireNonNull(cvh1).reels_video.pause();
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    public static MainActivity getInstance() { return instance; }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        if(introPref.isFirstTime()) {
            super.onBackPressed();
        }

        super.onResume();

        if(mode_changed == 1) {
            mode_changed = 0;
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }

        if(viewPager.getCurrentItem() != 2 && mRecyclerView != null) {
            RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
            int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
            int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

            if (firstVisiblePosition >= 0) {
                for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                    final RecyclerView.ViewHolder holder1 = mRecyclerView.findViewHolderForAdapterPosition(i);
                    ClipsFragment.ProgrammingViewHolder cvh1 = (ClipsFragment.ProgrammingViewHolder) holder1;
                    Objects.requireNonNull(cvh1).reels_video.pause();
                }
            }
        }

        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifCountInApp();
            }
        };
        registerReceiver(myReceiver, new IntentFilter(MessagingService.INTENT_FILTER));
        /////////////ADD NOTIFICATION BADGE IN MENU ITEM//////////////////

        if(FeedsFragment.changed == 1){
            viewPager.setCurrentItem(1);
        }
        else if(CommitteeFragment.changed == 1){
            viewPager.setCurrentItem(0);
        }

        final NotifCount[] notifCount = {new NotifCount()};
        docref3.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot=task.getResult();
                if(Objects.requireNonNull(documentSnapshot).exists()){
                    notifCount[0] = documentSnapshot.toObject(NotifCount.class);
                    MessagingService.nCount = Integer.toString(Objects.requireNonNull(notifCount[0]).getNotifCount());
                    if(Integer.parseInt(MessagingService.nCount) > 0) {
                        notifDot.setVisibility(View.VISIBLE);
                    }
                    notifDot.setText(MessagingService.nCount);
                }
            }
        });

        introPref = new IntroPref(MainActivity.this);
        String USERNAME = introPref.getFullName();
        String PROFILEPIC = introPref.getUserdp();
        TYPE = introPref.getType();
        GENDER = introPref.getGender();

        navigationView = findViewById(R.id.nav_view);
        View hView;
        hView = navigationView.getHeaderView(0);
        name = hView.findViewById(R.id.nav_Name);
        displaypic = hView.findViewById(R.id.displaypic);

        toolbarImage= findViewById(R.id.toolbarimg1);

        name.setText(USERNAME);

        if(PROFILEPIC !=null){

                Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
                builder.downloader(new OkHttp3Downloader(getApplicationContext(), Integer.MAX_VALUE));
                Picasso built = builder.build();
                built.setIndicatorsEnabled(true);
                built.setLoggingEnabled(true);
                Picasso.get()
                        .load(PROFILEPIC)
                        .error(R.drawable.image_background_grey)
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(displaypic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        if(GENDER!=null){
                            if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                                displaypic.setImageResource(R.drawable.ic_female);
                            }
                            else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                                displaypic.setImageResource(R.drawable.ic_male);
                            }
                            else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                                displaypic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                        else{
                            displaypic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
//                        Picasso.get().load(PROFILEPIC).into(displaypic);
                    }
                });
        }
        else {
            if(GENDER!=null){
                if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                    displaypic.setImageResource(R.drawable.ic_female);
                }
                else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                    displaypic.setImageResource(R.drawable.ic_male);
                }
                else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                    displaypic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
            else{
                displaypic.setImageResource(R.drawable.ic_account_circle_black_24dp);
            }
        }

        if(TYPE.matches("indi")){
            com_data.setVisibility(View.GONE);
        }
        else if(TYPE.matches("com")){
            com_data.setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance().collection("Users")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            BaseUserModel baseUserModel = Objects.requireNonNull(task.getResult()).toObject(BaseUserModel.class);
                            if(Objects.requireNonNull(baseUserModel).getPujoVisits() > 1) {
                                if(baseUserModel.getPujoVisits() > 1000) {
                                    visits.setText(baseUserModel.getPujoVisits()/1000 + "." + (baseUserModel.getPujoVisits() % 1000)/100 + "K");
                                } else {
                                    visits.setText(baseUserModel.getPujoVisits() + "");
                                }
                            } else {
                                visits.setText(baseUserModel.getPujoVisits() + "");
                            }
                            if(baseUserModel.getUpvoteL() != null){
                                if(baseUserModel.getUpvoteL().size() == 0){
                                    upvoters.setText("0");
                                }
                                else {
                                    upvoters.setText(baseUserModel.getUpvoteL().size()+"");
                                }
                            }
                            else {
                                upvoters.setText("0");
                            }
                        }
                    })
                    .addOnFailureListener(e -> { });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.theme) {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.dialog_set_theme);
            dialog.setCanceledOnTouchOutside(true);

            RadioButton system_default = dialog.findViewById(R.id.system_default);
            RadioButton light_mode = dialog.findViewById(R.id.light_mode);
            RadioButton dark_mode = dialog.findViewById(R.id.dark_mode);

            if(introPref.getTheme() == 1) {
                system_default.setChecked(true);
                light_mode.setChecked(false);
                dark_mode.setChecked(false);
            }
            else if(introPref.getTheme() == 2) {
                light_mode.setChecked(true);
                system_default.setChecked(false);
                dark_mode.setChecked(false);
            }
            else {
                dark_mode.setChecked(true);
                system_default.setChecked(false);
                light_mode.setChecked(false);
            }

            system_default.setOnClickListener(v -> {
                system_default.setChecked(true);
                light_mode.setChecked(false);
                dark_mode.setChecked(false);
            });

            light_mode.setOnClickListener(v -> {
                light_mode.setChecked(true);
                system_default.setChecked(false);
                dark_mode.setChecked(false);
            });

            dark_mode.setOnClickListener(v -> {
                dark_mode.setChecked(true);
                system_default.setChecked(false);
                light_mode.setChecked(false);
            });

            dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());

            dialog.findViewById(R.id.update).setOnClickListener(v-> {
                if(system_default.isChecked()) {
                    introPref.setTheme(1);
                    FirebaseFirestore.getInstance().document("Mode/night_mode")
                            .get().addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
                                    if(task.getResult().getBoolean("night_mode")) {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                }
                        new Handler().postDelayed(() -> {
                            dialog.dismiss();
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }, 200);
                    });
                }
                else if(light_mode.isChecked()) {
                    introPref.setTheme(2);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    new Handler().postDelayed(() -> {
                        dialog.dismiss();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }, 200);
                }
                else if(dark_mode.isChecked()) {
                    introPref.setTheme(3);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    new Handler().postDelayed(() -> {
                        dialog.dismiss();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }, 200);
                }

            });

            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
        else if (id == R.id.nav_logout) {
            new Handler().postDelayed(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Log out")
                        .setMessage("Do you want to continue?")
                        .setPositiveButton("Log out", (dialog, which) -> {
                            introPref.setFullName(null);
                            introPref.setUserdp(null);
                            introPref.setType(null);
                            introPref.setGender(null);

                            FirebaseFirestore.getInstance()
                                    .collection("Users/" + FirebaseAuth.getInstance().getUid() + "/AccessToken/")
                                    .document("Token").delete()
                                    .addOnCompleteListener(task -> {
                                        FirebaseAuth.getInstance().signOut();
                                        mGooglesigninclient.signOut();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();

            }, 200);
        }
        else if(id == R.id.nav_select_lang){
            new Handler().postDelayed(() -> {
                drawer.closeDrawers();
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_select_language);
                dialog.setCanceledOnTouchOutside(true);

                RadioButton bangla = dialog.findViewById(R.id.bangla);
                RadioButton english = dialog.findViewById(R.id.english);
                RadioButton hindi = dialog.findViewById(R.id.hindi);

                if(introPref.getLanguage().matches("en")) {
                    english.setChecked(true);
                    bangla.setChecked(false);
                    hindi.setChecked(false);
                }
                else if(introPref.getLanguage().matches("bn")) {
                    bangla.setChecked(true);
                    english.setChecked(false);
                    hindi.setChecked(false);
                }
                else if(introPref.getLanguage().matches("hi")) {
                    hindi.setChecked(true);
                    english.setChecked(false);
                    bangla.setChecked(false);
                }

                english.setOnClickListener(v -> {
                    english.setChecked(true);
                    bangla.setChecked(false);
                    hindi.setChecked(false);
                });

                bangla.setOnClickListener(v -> {
                    bangla.setChecked(true);
                    english.setChecked(false);
                    hindi.setChecked(false);
                });

                hindi.setOnClickListener(v -> {
                    hindi.setChecked(true);
                    english.setChecked(false);
                    bangla.setChecked(false);
                });

                dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());

                dialog.findViewById(R.id.done).setOnClickListener(v-> {
                    if(english.isChecked()) {
                        introPref.setLanguage("en");
                    }
                    else if(bangla.isChecked()) {
                        introPref.setLanguage("bn");
                    }
                    else if(hindi.isChecked()) {
                        introPref.setLanguage("hi");
                    }

                    new Handler().postDelayed(() -> {
                        dialog.dismiss();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }, 200);
                });

                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            },200);

        }
        else if(id == R.id.nav_tellafrnd){
            new Handler().postDelayed(() -> {
                drawer.closeDrawers();
                Intent i=new Intent();
                i.setAction(Intent.ACTION_SEND);
                String text = getResources().getString(R.string.share_with_friends);
                i.putExtra(Intent.EXTRA_TEXT,text);
                i.setType("text/plain");
                startActivity(Intent.createChooser(i,"Share with"));
            },200);
        }

        else if(id == R.id.whatsapp){
            new Handler().postDelayed(() -> {
                drawer.closeDrawers();
                String link = "https://www.applex.in/utsav-app/profile/" + introPref.getFullName().replace(" ", "_") + "/" + FirebaseAuth.getInstance().getUid();

                String text="Hi Utsav Admin! \n\nThis is *"+introPref.getFullName()+"* from Utsav App : Virtual Festivals.\n\n"+"Profile Link :\n"+link;

                String url = "https://api.whatsapp.com/send?phone=91"+WHATSAPP_NUMBER+"&text="+text;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            },200);
        }

        else if(id == R.id.nav_contact){
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, Webview.class);
                intent.putExtra("text","https://applex.in/contact/");
                intent.putExtra("bool","1");
                startActivity(intent);
            },200);
        }
        else if(id == R.id.nav_privacy){
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, Webview.class);
                intent.putExtra("text","https://applex.in/utsav-app-privacy-policy/");
                intent.putExtra("bool","2");
                startActivity(intent);
            },200);
        }
        else if(id == R.id.nav_about){
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, AboutUs.class);
                intent.putExtra("text","https://applex.in/contact/");
                intent.putExtra("bool","1");
                startActivity(intent);
            },200);
        }
        else if(id== R.id.nav_rate_us){
            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    public class GetLatestVersion extends AsyncTask<Void, Void, Void> {

        private String latestVersion;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + getPackageName())
                        .timeout(8000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select(".hAyfc .htlgb")
                        .get(7)
                        .ownText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(currentVersion != null && latestVersion != null) {
                ////////////////////FOR THE DEVELOPERS/////////////////////
                if(Integer.parseInt(currentVersion.replace('.', '0')) > Integer.parseInt(latestVersion.replace('.', '0'))) {
                    Log.i("DICKEYDICK", "FUCK YOU");
                }
                ////////////////////FOR THE DEVELOPERS/////////////////////
                else if(!currentVersion.equalsIgnoreCase(latestVersion)) {

                    MaterialDialog dialog = DialogUtils.getInstance().createAnimationDialog4(MainActivity.this);
                    dialog.setCanceledOnTouchOutside(true);

                    TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                    TextView update = (TextView) dialog.findViewById(R.id.update);

                    cancel.setOnClickListener(v -> dialog.dismiss());

                    update.setOnClickListener(v -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                        startActivity(intent);
                    });

                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            }
        }
    }

    public void notifCountInApp() {
        if(MessagingService.nCount == null || MessagingService.nCount.matches("0") ) {
            notifDot.setVisibility(View.GONE);
        }
        else {
            notifDot.setVisibility(View.VISIBLE);
        }
        notifDot.setText(MessagingService.nCount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        }
        else {
            if(doubleBackPressed) {
                introPref.setIsFirstTime(true);
                super.onBackPressed();
                return;
            }
            ////////////////RATE US/////////////////
            final int[] count = {introPref.getCount()};
            if(count[0] != -100) {
                if(count[0] >= 250) {
                    count[0] = 0;
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.dialog_rate_us);
                    dialog.setCanceledOnTouchOutside(true);

                    TextView remind = dialog.findViewById(R.id.remind);
                    TextView rate = dialog.findViewById(R.id.rate);

                    dialog.setOnCancelListener(dialog1 -> {
                        dialog1.dismiss();
                        count[0] = 100;
                        introPref.setCount(count[0]);
                    });

                    remind.setOnClickListener(v -> {
                        dialog.dismiss();
                        count[0] = 170;
                        introPref.setCount(count[0]);
                    });

                    rate.setOnClickListener(v -> {
                        count[0] = -100;
                        introPref.setCount(count[0]);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                        startActivity(intent);
                        dialog.dismiss();
                    });

                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
                else {
                    count[0] = count[0] + 1;
                    introPref.setCount(count[0]);
                }
            }
            ////////////////RATE US/////////////////
            doubleBackPressed = true;
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackPressed = false, 3000);
        }
    }
}