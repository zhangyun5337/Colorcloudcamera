package com.zhangyun.colorcloudcamera;

import android.content.Context;
import android.graphics.Color;
import android.provider.SyncStateContract;
import android.view.View;
import com.zhangyun.colorcloudcamera.utils.SharedDataUtil;
import com.flyco.animation.Attention.Swing;
import com.flyco.animation.ZoomExit.ZoomOutExit;
import com.flyco.dialog.utils.CornerUtils;
import com.flyco.dialog.widget.base.BaseDialog;
import com.google.android.cameraview.CameraView;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.suke.widget.SwitchButton;
import com.suke.widget.SwitchButton.OnCheckedChangeListener;
import java.lang.ref.WeakReference;

public class SettingDialog extends BaseDialog<SettingDialog> {
    SwitchButton btnAutoFocus = null;
    SwitchButton btnDili = null;
    SingleSelectToggleGroup btnQuality = null;
    SwitchButton btnTime = null;
    WeakReference<CameraView> weakReference = null;

    public SettingDialog(Context context, CameraView cameraView) {
        super(context);
        this.weakReference = new WeakReference(cameraView);
        getWindow().clearFlags(2);
    }

    public View onCreateView() {
        widthScale(0.85f);
        showAnim(new Swing());
        dismissAnim(new ZoomOutExit());
        View inflate = View.inflate(getContext(), R.layout.activity_setting_dialog, null);
        this.btnAutoFocus = (SwitchButton) inflate.findViewById(R.id.btnAutoFocus);
        this.btnQuality = (SingleSelectToggleGroup) inflate.findViewById(R.id.btnQuality);
        this.btnDili = (SwitchButton) inflate.findViewById(R.id.btnDili);
        this.btnTime = (SwitchButton) inflate.findViewById(R.id.btnTime);
        inflate.setBackgroundDrawable(CornerUtils.cornerDrawable(Color.parseColor("#66000000"), (float) dp2px(5.0f)));
        return inflate;
    }

    public void setUiBeforShow() {
        this.btnAutoFocus.setChecked(((CameraView) this.weakReference.get()).getAutoFocus());
        this.btnAutoFocus.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                ((CameraView) SettingDialog.this.weakReference.get()).setAutoFocus(b);
                SharedDataUtil.saveData(SettingDialog.this.getContext(), Constants.CAMERA_AUTOFOCUS, Boolean.valueOf(b));
            }
        });
        int cq = ((Integer) SharedDataUtil.getData(getContext(), Constants.CAMERA_QUALITY, Integer.valueOf(60))).intValue();
        if (cq == 30) {
            this.btnQuality.check(R.id.choice_a);
        } else if (cq == 60) {
            this.btnQuality.check(R.id.choice_b);
        } else {
            this.btnQuality.check(R.id.choice_c);
        }
        this.btnQuality.setOnCheckedChangeListener(new SingleSelectToggleGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(SingleSelectToggleGroup singleSelectToggleGroup, int i) {
                if (i == R.id.choice_a) {
                    SharedDataUtil.saveData(SettingDialog.this.getContext(), Constants.CAMERA_QUALITY, Integer.valueOf(30));
                } else if (i == R.id.choice_b) {
                    SharedDataUtil.saveData(SettingDialog.this.getContext(), Constants.CAMERA_QUALITY, Integer.valueOf(60));
                } else {
                    SharedDataUtil.saveData(SettingDialog.this.getContext(), Constants.CAMERA_QUALITY, Integer.valueOf(90));
                }
            }
        });
        this.btnDili.setChecked(((Boolean) SharedDataUtil.getData(getContext(), Constants.WITH_DILI, Boolean.valueOf(true))).booleanValue());
        this.btnDili.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                SharedDataUtil.saveData(SettingDialog.this.getContext(), Constants.WITH_DILI, Boolean.valueOf(b));
            }
        });
        this.btnTime.setChecked(((Boolean) SharedDataUtil.getData(getContext(), Constants.WITH_TIME, Boolean.valueOf(true))).booleanValue());
        this.btnTime.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                SharedDataUtil.saveData(SettingDialog.this.getContext(), Constants.WITH_TIME, Boolean.valueOf(b));
            }
        });
    }
}

