package com.lzy.imagepicker.view;

import ohos.agp.components.AttrSet;
import ohos.agp.components.Checkbox;
import ohos.app.Context;
import ohos.media.audio.SoundPlayer;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：带声音的Checkbox
 * 修订历史：
 * ================================================
 */
public class SuperCheckBox extends Checkbox {

    private Context context;
    SoundPlayer soundPlayer = new SoundPlayer();

    public SuperCheckBox(Context context) {
        super(context);
        this.context = context;
    }

    public SuperCheckBox(Context context, AttrSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SuperCheckBox(Context context, AttrSet attrs, String styleName) {
        super(context, attrs, null);
        this.context = context;
    }

    @Override
    public boolean callOnClick() {

        return super.callOnClick();
    }


    @Override
    public boolean simulateClick() {

        final boolean handled = super.simulateClick();
        if (!handled) {
            soundPlayer.playSound(SoundPlayer.SoundType.KEY_CLICK);
        }
        return handled;
    }

}
