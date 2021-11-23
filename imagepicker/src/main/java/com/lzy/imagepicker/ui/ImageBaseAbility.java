package com.lzy.imagepicker.ui;


import com.lzy.imagepicker.ResourceTable;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.window.dialog.ToastDialog;
import ohos.bundle.IBundleManager;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageBaseAbility extends Ability {

    private ToastDialog dialog;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);

        getWindow().setStatusBarColor(ResourceTable.Color_ip_color_primary_dark);
    }


    public boolean checkPermission(String permission) {
        return verifySelfPermission(permission) == IBundleManager.PERMISSION_GRANTED;
    }

    public void showToast(String toastText) {
        if (dialog == null) {
            dialog = new ToastDialog(this);
        }
        if (dialog.isShowing()) {
            dialog.hide();
        }
        dialog.setText(toastText);
        dialog.setDuration(2_000);

        dialog.show();
    }

}
