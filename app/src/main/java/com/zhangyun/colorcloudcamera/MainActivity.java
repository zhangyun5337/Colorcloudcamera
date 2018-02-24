package com.zhangyun.colorcloudcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy.Builder;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.telephony.TelephonyManager;
import android.telecom.TelecomManager;
import android.telephony.CellLocation;
import android.telephony.CellInfo;
import android.widget.EditText;

import com.baidu.location.BDAbstractLocationListener;
import com.zhangyun.colorcloudcamera.utils.AlbumUtil;
import com.zhangyun.colorcloudcamera.utils.BitmapUtil;
import com.zhangyun.colorcloudcamera.utils.PositionUtil;
import com.zhangyun.colorcloudcamera.utils.PositionUtil.Gps;
import com.zhangyun.colorcloudcamera.utils.SharedDataUtil;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.google.android.cameraview.CameraView;
import com.google.android.cameraview.CameraView.Callback;
import com.makeramen.roundedimageview.RoundedImageView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.RationaleRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    static DateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static DecimalFormat nf = new DecimalFormat("#.000000");
    static DecimalFormat nf2 = new DecimalFormat("#.00");
    public static String photoFolder = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera");
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null && msg.what == 0) {
                List<String> images = AlbumUtil.getImagesInFolder(MainActivity.photoFolder,true);
                if (images == null || images.size() <= 0) {
                    MainActivity.this.mPreview.setImageResource(R.drawable.camera);
                } else {
                    MainActivity.this.mPreview.setImageBitmap(BitmapUtil.decodeSampledBitmapFromFd((String) images.get(0),MainActivity.this.mPreview.getWidth(),MainActivity.this.mPreview.getHeight()));
                }
            }
            if (msg != null && msg.what == 1) {
                MainActivity.this.showLocation(msg.obj);
            }
        }
    };

    CameraView mCameraView = null;
    public LocationClient mLocationClient = null;
    public AMapLocationClient mLocationClient2 = null;
    RoundedImageView mPreview = null;
    AppCompatTextView mTextView = null;
    public BDLocationListener myListener = new MyLocationListener();
    float scale = 1.0f;


    class SaveImageTask extends AsyncTask<Object, Void, File> {
        SaveImageTask() {
        }

        protected File doInBackground(Object[] objects) {
            try {
                File f = new File(MainActivity.photoFolder,"CMCC_" + MainActivity.df.format(new Date()) + ".png");
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                if (!f.exists()) {
                    f.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(f);
                String gText = String.valueOf(objects[1]);
                byte[] bufferData = (byte[]) objects[0];
                Bitmap bmp = BitmapFactory.decodeByteArray(bufferData,0,bufferData.length);
                Config bitmapConfig = bmp.getConfig();
                if (bitmapConfig == null) {
                    bitmapConfig = Config.ARGB_8888;
                }
                Bitmap bitmap = bmp.copy(bitmapConfig,true);
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint(1);
                paint.setColor(Color.RED);
                paint.setTextSize(Float.parseFloat(String.valueOf(50)) * MainActivity.this.scale);
                paint.setDither(true);
                paint.setFilterBitmap(true);
                Rect bounds = new Rect();
                float x = 16.0f * MainActivity.this.scale;
                float y = 16.0f * MainActivity.this.scale;
                for (String str : gText.split("\\\n")) {
                    paint.getTextBounds(str,0,str.length(),bounds);
                    canvas.drawText(str,x,((float) bounds.height()) + y,paint);
                    y += (float) bounds.height();
                }
                bitmap.compress(CompressFormat.JPEG,((Integer) SharedDataUtil.getData(MainActivity.this,Constants.CAMERA_QUALITY,Integer.valueOf(60))).intValue(),fos);
                fos.flush();
                fos.close();
                bmp.recycle();
                bitmap.recycle();
                System.gc();
                MainActivity.this.handler.sendEmptyMessage(0);
                Media.insertImage(MainActivity.this.getContentResolver(),f.getAbsolutePath(),f.getName(),f.getName());
                MainActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE",Uri.fromFile(f)));
                return f;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class MyLocationListener implements BDLocationListener {
        void onConnectHotSpotMessage(String s,int i) {
        }

        public void onReceiveLocation(BDLocation location) {

            MainActivity.this.handler.sendMessage(MainActivity.this.handler.obtainMessage(1,location));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024,1024);
        getSupportActionBar().hide();
        setContentView((int) R.layout.activity_main);
        if (VERSION.SDK_INT >= 24) {
            StrictMode.setVmPolicy(new Builder().build());
        }
        this.scale = getResources().getDisplayMetrics().density;
        this.mCameraView = (CameraView) findViewById(R.id.camera);
        this.mTextView = (AppCompatTextView) findViewById(R.id.mTextView);
        this.mPreview = (RoundedImageView) findViewById(R.id.mPreview);
        findViewById(R.id.takePhoto).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.takePhoto(view);
            }
        });
        this.mPreview.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.toGallery(view);
            }
        });
        findViewById(R.id.btnCameraLight).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.changeLight(view);
            }
        });
        findViewById(R.id.btnCameraSwitch).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.switchCamera(view);
            }
        });
        findViewById(R.id.setting).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.settingCamera(view);
            }
        });
        this.mCameraView.addCallback(new Callback() {
            public void onCameraOpened(CameraView cameraView) {
                super.onCameraOpened(cameraView);
                MainActivity.this.handler.postDelayed(new Runnable() {
                    public void run() {
                        MainActivity.this.handler.sendEmptyMessage(0);
                    }
                },1000);
            }

            public void onCameraClosed(CameraView cameraView) {
                super.onCameraClosed(cameraView);
            }

            public void onPictureTaken(CameraView cameraView,byte[] data) {
                super.onPictureTaken(cameraView,data);
                new SaveImageTask().execute(new Object[]{data,MainActivity.this.mTextView.getText() + "",Float.valueOf(MainActivity.this.mTextView.getTextSize())});
            }
        });

        ((RationaleRequest) ((RationaleRequest) ((RationaleRequest) AndPermission.with((Activity) this).requestCode(100)).permission("android.permission.CAMERA","android.permission.READ_PHONE_STATE","android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION","android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE")).rationale(new RationaleListener() {
            public void showRequestPermissionRationale(int i,Rationale rationale) {
                AndPermission.rationaleDialog(MainActivity.this,rationale).show();
            }
        }).callback(new PermissionListener() {
            public void onSucceed(int i,@NonNull List<String> list) {
                MainActivity.this.mLocationClient = new LocationClient(MainActivity.this.getApplicationContext());
                MainActivity.this.mLocationClient.registerLocationListener((BDLocationListener) MainActivity.this.myListener);
                MainActivity.this.initBaiDuLocation();
                MainActivity.this.mLocationClient2 = new AMapLocationClient(MainActivity.this.getApplicationContext());
                MainActivity.this.mLocationClient2.setLocationListener(MainActivity.this.mAMapLocationListener);
                MainActivity.this.handler.postDelayed(new Runnable() {
                    public void run() {

                    }
                },3000);
            }

            public void onFailed(int i,@NonNull List<String> list) {
                if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this,(List) list)) {
                    AndPermission.defaultSettingDialog(MainActivity.this,400).setTitle("权限申请失败").setMessage("您拒绝了应用运行必须的一些权限，已经没法愉快的玩耍了，请在设置中授权！").setPositiveButton("好，去设置").show();
                }
            }
        })).start();
    }

    protected void onResume() {
        super.onResume();

        this.mCameraView.setAutoFocus(((Boolean) SharedDataUtil.getData(this,Constants.CAMERA_AUTOFOCUS,Boolean.valueOf(true))).booleanValue());
        this.mCameraView.start();


    }

    protected void onPause() {
        this.mCameraView.stop();
        super.onPause();
    }

    protected void takePhoto(View view) {
        this.handler.post(new Runnable() {
            public void run() {
                MainActivity.this.mCameraView.takePicture();
            }
        });
    }

    protected void toGallery(View view) {
        List<String> images = AlbumUtil.getImagesInFolder(photoFolder,true);
        Intent intent = new Intent();
        intent.putExtra("PHOTO_FOLDER",photoFolder);
        intent.setClass(this,GalleryActivity.class);
        startActivity(intent);
    }

    protected void changeLight(View view) {
        if (this.mCameraView != null) {
            AppCompatImageView btn = (AppCompatImageView) view;
            int flashLight = this.mCameraView.getFlash();
            if (flashLight == CameraView.FLASH_AUTO) {
                this.mCameraView.setFlash(CameraView.FLASH_ON);
                btn.setImageResource(R.drawable.light_on);
            } else if (flashLight == CameraView.FLASH_ON) {
                this.mCameraView.setFlash(CameraView.FLASH_OFF);
                btn.setImageResource(R.drawable.light_off);
            } else if (flashLight == CameraView.FLASH_OFF) {
                this.mCameraView.setFlash(CameraView.FLASH_AUTO);
                btn.setImageResource(R.drawable.light_auto);
            }
        }
    }

    protected void switchCamera(View view) {
        if (this.mCameraView != null) {
            AppCompatImageView btn = (AppCompatImageView) view;
            if (this.mCameraView.getFacing() == CameraView.FACING_FRONT) {
                this.mCameraView.setFacing(CameraView.FACING_BACK);
            } else {
                this.mCameraView.setFacing(CameraView.FACING_FRONT);
            }
        }
    }

    protected void settingCamera(View view) {
        if (this.mCameraView != null) {
            new SettingDialog(this,this.mCameraView).show();
        }
    }

    public AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                MainActivity.this.handler.sendMessage(MainActivity.this.handler.obtainMessage(1,amapLocation));
            }
        }
    };

    private void showLocation(Object location) {
        TelephonyManager phoneMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String PhoneNumber = phoneMgr.getLine1Number();
        String  PhoneType = String.valueOf(phoneMgr.getCellLocation());







        if (location != null) {
            double lat = 0.0d;
            double lon = 0.0d;
            float accu = 0.0f;
            String addr = null;

            String gdan = null;
            EditText editText1 = (EditText)findViewById(R.id.editText);
            gdan=editText1.getText().toString();
            Gps gps;
            if (location instanceof BDLocation) {
                BDLocation bdLocation = (BDLocation) location;
                gps = PositionUtil.gcj_To_Gps84(bdLocation.getLatitude(), bdLocation.getLongitude());
                lat = gps.getWgLat();
                lon = gps.getWgLon();
                String addr2 = ((BDLocation) location).getAddrStr();
                addr = bdLocation.getAddrStr();
                accu = bdLocation.getRadius();
            } else if (location instanceof AMapLocation) {
                AMapLocation aMapLocation = (AMapLocation) location;
                if (aMapLocation.getErrorCode() == 0) {
                    gps = PositionUtil.gcj_To_Gps84(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    lat = gps.getWgLat();
                    lon = gps.getWgLon();
                    addr = ((AMapLocation) location).getAddress();
                    accu = ((AMapLocation) location).getAccuracy();
                }
            }
            String locationStr = "";
            if (((Boolean) SharedDataUtil.getData(this, Constants.RED_DILI, Boolean.valueOf(true))).booleanValue()) {
                String str;
                StringBuilder append = new StringBuilder().append(locationStr).append("纬度：").append(lat == 0.0d ? "--" : nf.format(lat)).append("\n经度：");
                if (lon == 0.0d) {
                    str = "--";
                } else {
                    str = nf.format(lon);
                }
                StringBuilder append2 = append.append(str).append("\n地址：");
                if (addr == null) {
                    addr = "--";
                }
                locationStr = append2.append(addr).append("\n精度：±").append(nf2.format((double) accu)).append("米").toString();
                locationStr =locationStr+"\n工单号："+gdan;
                locationStr =locationStr+"\n手机号码："+PhoneNumber;
                locationStr =locationStr+"\n网络信息："+PhoneType;
            }
            if (((Boolean) SharedDataUtil.getData(this, Constants.WITH_TIME, Boolean.valueOf(true))).booleanValue()) {
                locationStr = locationStr + "\n时间：" + df2.format(new Date());
            }


            this.mTextView.setText(locationStr);
            this.mTextView.setTextColor(this.getResources().getColor(R.color.red));
        }
    }

    protected void onDestroy() {
        if (this.mLocationClient != null) {
            this.mLocationClient.stop();
        }
        if (this.mLocationClient2 != null) {
            this.mLocationClient2.stopLocation();
        }
        super.onDestroy();
    }

    private void initBaiDuLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(false);
        option.setIsNeedLocationPoiList(false);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        this.mLocationClient.setLocOption(option);
        this.mLocationClient.start();
    }

    private void initGaoDeLocation() {
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(1000);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setMockEnable(false);
        mLocationOption.setHttpTimeOut(20000);
        mLocationOption.setLocationCacheEnable(false);
        this.mLocationClient2.setLocationOption(mLocationOption);
        this.mLocationClient2.startLocation();
    }





}

