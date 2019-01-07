package com.ermile.khadijeh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.ermile.khadijeh.network.AppContoroler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Intro extends AppCompatActivity {

    public Handler mHandler;
    public boolean continue_or_stop;

    ViewpagersAdapter PagerAdapter;  // for View page
    ViewPager viewpager; //  for dots & Button in XML
    private LinearLayout dotsLayout; // dots in XML
    private TextView[] dots; // for add dots
    public int count = 4; // Slide number
    private Button btnSkip, btnNext ; // Button in XML
    private first_oppen prefManager; // Checking for first time launch


    /**
     * onCreate Method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new NetCheck().execute();
        // Checking for first time launch - before calling setContentView
        prefManager = new first_oppen(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        // JSON Methods
        StringRequest stringRequest3 = new StringRequest(Request.Method.GET, "http://mimsg.ir/json_app/app.json", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject mainObject = new JSONObject(response);
                    Boolean fullscreen = mainObject.getBoolean("fullscreen");
                    if (fullscreen == true)
                    {
//                        hideSystemUI();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {  // Error NET
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });AppContoroler.getInstance().addToRequestQueue(stringRequest3);


        // XML
        setContentView(R.layout.intro);
        // Chang ID XML
        dotsLayout = findViewById(R.id.layoutDots); // OOOO
        btnNext = findViewById(R.id.btn_next); //  NEXT
        btnSkip = findViewById(R.id.btn_skip); //  SKIP
        viewpager = findViewById(R.id.view_pagers); // view page in XML
        // set
        PagerAdapter = new ViewpagersAdapter(this); // add Adapter (in line 55)
        viewpager.setAdapter(PagerAdapter); // set Adapter to View pager in XML
        viewpager.addOnPageChangeListener(viewPagerPageChangeListener); // for dots next page
        // adding bottom dots
        addBottomDots(0);
        // making notification bar transparent
        changeStatusBarColor();
        // set On Click Listener
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < count ) {
                    // move to next screen
                    viewpager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });



    }

    /**
     * Change Number page & Dots
     */
    private void addBottomDots(int currentPage) {

        dots = new TextView[count];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }
    private int getItem(int i) {
        return viewpager.getCurrentItem() + i;
    }

    // launch Home = go to new activity
    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(Intro.this , MainActivity.class) );
        finish();
    }

    /**
     * change listener in Last Page
     */
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == count - 1) {
                // last page. make button text to GOT IT
                btnNext.setText("ورود به سرشمار");
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText("بعدی");
                btnSkip.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) { }
        @Override
        public void onPageScrollStateChanged(int arg0) { }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * This Moder Adapter
     * View Pager Adapter
     */
    public class ViewpagersAdapter extends PagerAdapter {

        private Context context;
        private LayoutInflater inflater;
        ViewpagersAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount()
        {
            return count;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            // Static Methods
            inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            final View view = inflater.inflate(R.layout.intro_item , container , false);
            // My item for view slide in XML
            final TextView title = view.findViewById(R.id.txv_title);
            final TextView des = view.findViewById(R.id.txv_des);
            final LinearLayout color_bg = view.findViewById(R.id.background_slide);
            final ImageView imgview = view.findViewById(R.id.img_slide);

            final TextView url = view.findViewById(R.id.url);

            final ProgressBar progress_slide = view.findViewById(R.id.progress_slide);

            if (imgview != null){
                progress_slide.setVisibility(View.GONE);
            }

            int languish = 2;

            switch (languish){
                case 1:
                    url.setText("https://khadije.com/api/v5/detail");
                    break;
                case 2:
                    url.setText("https://khadije.com/ar/api/v5/detail");
                    break;
                case 3:
                    url.setText("https://khadije.com/ar/api/v5/detail");
                    break;
            }




            // JSON Methods
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url.getText().toString(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // get objects
                        JSONObject intro = response.getJSONObject("intro");
                        JSONArray intro_slide = intro.getJSONArray("slide");

                        int countAll = intro_slide.length();

                        int j        = countAll - 1;

                        String[] intro_title      = new String[countAll];
                        String[] intro_desc       = new String[countAll];
                        String[] intro_background = new String[countAll];
                        String[] intro_image      = new String[countAll];

                        for(int i = 0; i <= j; i++)
                        {
                            JSONObject temp_intro = intro_slide.getJSONObject(i);
                            intro_title[i]        =  temp_intro.getString("title");
                            intro_desc[i]         =  temp_intro.getString("desc");
                            intro_background[i]   =  temp_intro.getString("background");
                            intro_image[i]        =  temp_intro.getString("image");
                        }
                        // Title
                        title.setText(intro_title[position]);
                        // Description
                        des.setText(intro_desc[position]);
                        // Color Background
                        color_bg.setBackgroundColor(Color.parseColor(intro_background[position]));
                        // Image
                        Glide.with(context).load(intro_image[position]).into(imgview);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {  // Error NET
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress_slide.setVisibility(View.VISIBLE);
                    title.setVisibility(View.GONE);
                    des.setVisibility(View.GONE);
                    imgview.setVisibility(View.GONE);
                }
            });AppContoroler.getInstance().addToRequestQueue(req);
            // END JSON
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView( (LinearLayout) object);
        }
    }

    /**
     * Async Task to check whether internet connection is working.
     **/

    private class NetCheck extends AsyncTask<String,String,Boolean>
    {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        /**
         * Gets current device state and checks for working internet connection by trying Google.
         **/
        @Override
        protected Boolean doInBackground(String... args){

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }
        @Override
        protected void onPostExecute(Boolean th){

            if(th == true){
            }
            else{
                View parentLayout = findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar.make(parentLayout, "به اینترنت متصل شوید", Snackbar.LENGTH_INDEFINITE).setDuration(999999999)
                        .setAction("تلاش مجدد", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new Intro.NetCheck().execute();
                                finish();
                                startActivity(getIntent());
                            }
                        });
                snackbar.setActionTextColor(Color.RED);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();

            }
        }
    }

}

