package com.sharprogramming.xkcd;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.channels.AsynchronousChannel;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private static RequestQueue requestQueue;

    final String FAVORITES_KEY = "com.sharprogramming.xkcd.favorites";
    @Nullable
    public static RequestQueue getRequestQueue(){
        return requestQueue;
    }

    int maxComic;
    private int minDownloadedComic;
    TreeMap<Integer, XKCDcomic> favorites = new TreeMap<>();
    TreeMap<Integer, XKCDcomic> all = new TreeMap<>();
    private CartoonAdapter cartoonAdapter = new CartoonAdapter();
    private boolean favoriteMode = false;

    Group noFavGroup;
    private ImageView backbutton;
    private TextView favoriteCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.util.Log.d("JSON01", "started ");
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
//        AsyncTask.execute(() ->{
        cartoonAdapter.setComics(all);
        XKCDcomic.getComic(XKCDcomic.MOST_RECENT_COMIC, response -> {
            maxComic = response.getNum();
            minDownloadedComic = maxComic;
//            cartoonAdapter.notifyDataSetChanged();
            all.put(response.getNum(), response);
            downloadMoreComics(10);
            cartoonAdapter.notifyDataSetChanged();
        }, error -> android.util.Log.e("RequestError", error.getMessage() + ""));
//        });
        setContentView(R.layout.activity_main);
        RecyclerView rv = findViewById(R.id.cartoon_list);
        backbutton = findViewById(R.id.back);
        favoriteCount = findViewById(R.id.countView);
        noFavGroup = findViewById(R.id.no_fav_group);
        backbutton.setOnClickListener(view -> setFavoriteMode(false));
        findViewById(R.id.favoriteLayout).setOnClickListener(view -> setFavoriteMode(true));
//        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FAVORITES_KEY, "").apply();
        String favoriteJSON =  PreferenceManager.getDefaultSharedPreferences(this).getString(FAVORITES_KEY, null);
        android.util.Log.d("JSON01", "json " + favoriteJSON);
       // favorites = new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(this).getString("com.sharprogramming.xkcd.favorites", ""), new TypeToken<HashMap<Integer, XKCDcomic>>(){}.getType());
        favorites = favoriteJSON == null || favoriteJSON.isEmpty() ? new TreeMap<>() : new Gson().fromJson(favoriteJSON, new TypeToken<TreeMap<Integer, XKCDcomic>>(){}.getType());
        setFavoriteCount();
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(cartoonAdapter);
    }

    @Override
    protected void onPause() {
        String favoritesString = new Gson().toJson(favorites);
        android.util.Log.d("JSON01", "saving " + favoritesString);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FAVORITES_KEY, favoritesString).apply();
        super.onPause();
    }

    private void setFavoriteCount() {
        if (favorites.size() > 99){
            favoriteCount.setText("99+");
        }else {
            favoriteCount.setText(String.valueOf(favorites.size()));
        }
    }

    int unfinshedJobs;
    private void downloadMoreComics(int amount) {
        for(int i = minDownloadedComic - 1; i >= minDownloadedComic - amount; i--){
            unfinshedJobs++;
            XKCDcomic.getComic(i, response -> {
                all.put(response.getNum(), response);

                android.util.Log.d("UF", "Unfinished jobs " + unfinshedJobs + " getNum " + response.getNum());
                finishJob(amount);
            }, error -> android.util.Log.e("RequestError", error.getMessage() + ""));
        }
    }

    private synchronized void finishJob(int amount){
        android.util.Log.d("UF", "Unfinished jobs " + unfinshedJobs);
        if (--unfinshedJobs == 0){
            minDownloadedComic = minDownloadedComic - amount;
            cartoonAdapter.notifyDataSetChanged();
        }
    }


    private void setFavoriteMode(boolean on){
        favoriteMode = on;
        if (on){
            backbutton.setVisibility(View.VISIBLE);
            noFavGroup.setVisibility(View.VISIBLE);
            cartoonAdapter.setComics(favorites);
        }else {
            backbutton.setVisibility(View.GONE);
            noFavGroup.setVisibility(View.GONE);
            cartoonAdapter.setComics(all);
        }
    }
    class CartoonAdapter extends RecyclerView.Adapter<CartoonAdapter.ViewHolder>{

        Map<Integer, XKCDcomic> comics;

        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cartoon_item, parent, false);
            return new ViewHolder(v);
        }

        private void setComics(Map<Integer, XKCDcomic> comics){
            this.comics = comics;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            android.util.Log.d("XKCD math", "maxComic " + maxComic + " position " + position + " min comic " + minDownloadedComic );
            XKCDcomic comic = favoriteMode ? favorites.get(favorites.keySet().toArray()[favorites.size() - position - 1]): all.get(maxComic - position);
            holder.setComic(comic);

            // If we are in regular mode, and our array only has 2 cartoons left, and there are none being fetched at the moment , it is time to fetch more from the server
            if (!favoriteMode && all.size() - position < 2 && unfinshedJobs == 0 ){
                downloadMoreComics(10);
            }
        }

        @Override
        public int getItemCount() {
            if (favoriteMode){
                return favorites.size();
            }else {
                return all.size();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            XKCDcomic comic;
            TextView titleView, number_date_view, alt_text_view;
            ImageView cartoonView, favoriteView;
            ViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.titleView);
                number_date_view = itemView.findViewById(R.id.number_date_view);
                alt_text_view = itemView.findViewById(R.id.alt_text_view);
                cartoonView = itemView.findViewById(R.id.cartoon_view);
                favoriteView = itemView.findViewById(R.id.favoriteView);
                favoriteView.setOnClickListener(v -> {
                    makeFavorite(favorites.get(comic.getNum()) == null);
                });
            }

            void makeFavorite(boolean on){
                if (on){
                    favorites.put(comic.getNum(), comic);
                    favoriteView.setAlpha(1.0f);
                }else{
                    favorites.remove(comic.getNum());
                    favoriteView.setAlpha(0.25f);
                }
                setFavoriteCount();
            }
            void setComic(XKCDcomic comic){
//                if (favorites.get(comic.getNum()) != null){
//
//                }else {
                    Glide.with(MainActivity.this).applyDefaultRequestOptions(new RequestOptions().override(Target.SIZE_ORIGINAL).transform(new BitmapTransformation() {
                        @Override
                        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                            // Force density to 320 dpi as this the retina display density
                            toTransform.setDensity(320);
                            return toTransform;
                        }

                        @Override
                        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

                        }
                    })).load(comic.getImgRetina()).into(cartoonView);
//                }
                favoriteView.setAlpha(favorites.get(comic.getNum()) == null ? 0.25f : 1.0f);
                titleView.setText(comic.getTitle());
                alt_text_view.setText(comic.getAlt());
                Calendar calendar = new GregorianCalendar(comic.getYear(), comic.getMonth(), comic.getDay());
                Date newDate = calendar.getTime();
                DateFormat dateFormat =  SimpleDateFormat.getDateInstance();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMM d yyyy");
                number_date_view.setText(MainActivity.this.getString(R.string.num_date, comic.getNum(), dateFormat.format(newDate)));

                this.comic = comic;
            }
        }
    }
}
