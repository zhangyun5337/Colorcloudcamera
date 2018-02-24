package com.zhangyun.colorcloudcamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy.Builder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import com.zhangyun.colorcloudcamera.utils.AlbumUtil;
import com.bumptech.glide.Glide;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.github.chrisbanes.photoview.PhotoView;
import com.sackcentury.shinebuttonlib.ShineButton;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    LargePicturesPargeAdapter adapter = null;
    List<String> images = new LinkedList();
    String photoFolder = "";
    ViewPager viewPager = null;

    public class LargePicturesPargeAdapter extends PagerAdapter {
        private List<String> allPicturePaths;
        private Context mContext;

        public LargePicturesPargeAdapter(Context context, List<String> allPicturePaths) {
            this.mContext = context;
            this.allPicturePaths = allPicturePaths;
        }

        public void setData(List<String> allPicturePaths) {
            this.allPicturePaths = allPicturePaths;
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.allPicturePaths.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new PhotoView(this.mContext);
            imageView.setLayoutParams(new LayoutParams(-1, -1));
            ((ViewPager) container).addView(imageView);
            Glide.with(this.mContext).load(new File((String) this.allPicturePaths.get(position))).into(imageView);
            return imageView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            View imageView = (PhotoView) object;
            if (imageView != null) {
                Glide.with(this.mContext).clear(imageView);
                ((ViewPager) container).removeView(imageView);
            }
        }
    }

    protected void initImages() {
        this.images.clear();
        this.images = AlbumUtil.getImagesInFolder(this.photoFolder, true);
        if (this.images == null) {
            this.images = new LinkedList();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView((int) R.layout.activity_gallery);
        if (VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.setStatusBarColor(Color.parseColor("#FF2B2B2B"));
            window.setNavigationBarColor(Color.parseColor("#FF2B2B2B"));
        }
        if (VERSION.SDK_INT >= 24) {
            StrictMode.setVmPolicy(new Builder().build());
        }
        this.photoFolder = getIntent().getStringExtra("PHOTO_FOLDER");
        initImages();
        this.viewPager = (ViewPager) findViewById(R.id.viewPager);
        this.adapter = new LargePicturesPargeAdapter(this, this.images);
        this.viewPager.setAdapter(this.adapter);
        this.viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ((AppCompatTextView) GalleryActivity.this.findViewById(R.id.positionView)).setText((position + 1) + " / " + GalleryActivity.this.images.size());
            }

            public void onPageSelected(int position) {
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                GalleryActivity.this.finish();
            }
        });
        findViewById(R.id.btnAlbum).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                File file = new File((String) GalleryActivity.this.images.get(GalleryActivity.this.viewPager.getCurrentItem()));
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                GalleryActivity.this.startActivity(intent);
            }
        });
        findViewById(R.id.btnShare).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((ShineButton) view).setChecked(false);
                view.postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = new Intent("android.intent.action.SEND");
                        File f = new File((String) GalleryActivity.this.images.get(GalleryActivity.this.viewPager.getCurrentItem()));
                        if (f != null && f.exists() && f.isFile()) {
                            intent.setType("image/jpg");
                            intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(f));
                            intent.setFlags(268435456);
                            GalleryActivity.this.startActivity(intent);
                        }
                    }
                }, 400);
            }
        });
        findViewById(R.id.btnDelete).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((ShineButton) view).setChecked(false);
                final NormalDialog dialog = new NormalDialog(GalleryActivity.this);
                ((NormalDialog) ((NormalDialog) ((NormalDialog) ((NormalDialog) ((NormalDialog) ((NormalDialog) ((NormalDialog) dialog.isTitleShow(false)).cornerRadius(5.0f)).content("是否确删除该图片?")).contentGravity(17)).widthScale(0.85f)).showAnim(new BounceTopEnter())).dismissAnim(new SlideBottomExit())).show();
                dialog.setOnBtnClickL(new OnBtnClickL() {
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                }, new OnBtnClickL() {
                    public void onBtnClick() {
                        dialog.dismiss();
                        int currentItem = GalleryActivity.this.viewPager.getCurrentItem();
                        File file;
                        if (GalleryActivity.this.images.size() == 1) {
                            file = new File((String) GalleryActivity.this.images.get(currentItem));
                            if (file.exists()) {
                                file.delete();
                            }
                            GalleryActivity.this.finish();
                            return;
                        }
                        int oldLength = GalleryActivity.this.images.size();
                        String delPath = (String) GalleryActivity.this.images.remove(currentItem);
                        GalleryActivity.this.adapter.setData(GalleryActivity.this.images);
                        AppCompatTextView positionView = (AppCompatTextView) GalleryActivity.this.findViewById(R.id.positionView);
                        if (currentItem == oldLength - 1) {
                            GalleryActivity.this.viewPager.setCurrentItem(currentItem - 1, true);
                            positionView.setText(currentItem + " / " + currentItem);
                        } else {
                            GalleryActivity.this.viewPager.setCurrentItem(currentItem, true);
                            positionView.setText((currentItem + 1) + " / " + (oldLength - 1));
                        }
                        file = new File(delPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                });
            }
        });
    }
}
