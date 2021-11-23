package com.lzy.imagepicker.view;


import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.ResourceTable;
import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorGroup;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.BaseDialog;
import ohos.agp.window.dialog.PopupDialog;
import ohos.app.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：16/8/1
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class FolderPopUpWindow extends PopupDialog implements Component.ClickedListener, BaseDialog.DialogListener {

    private ListContainer listView;
    private OnItemClickListener onItemClickListener;
    private final Component masker;
    private final Component marginView;
    private int marginPx;

    public FolderPopUpWindow(Context context, BaseItemProvider provider) {
        super(context, null);
        LayoutScatter scatter = LayoutScatter.getInstance(context);
        final Component component = scatter.parse(ResourceTable.Layout_pop_folder, null, false);
        masker = component.findComponentById(ResourceTable.Id_masker);
        masker.setClickedListener(this);
        marginView = component.findComponentById(ResourceTable.Id_margin);
        marginView.setClickedListener(this);
        listView = (ListContainer) component.findComponentById(ResourceTable.Id_listView);
        listView.setItemProvider(provider);

        setCustomComponent(component);
        //如果不设置，就是 AnchorView 的宽度
        ComponentContainer.LayoutConfig layoutConfig = new ComponentContainer.LayoutConfig(ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT);
        listView.setLayoutConfig(layoutConfig);
        listView.setFocusable(Component.FOCUS_ENABLE);
        setBackColor(new Color(0));

        //setAnimationStyle(0);
        component.getComponentTreeObserver().addTreeLayoutChangedListener(new ComponentTreeObserver.GlobalLayoutListener() {
            @Override
            public void onGlobalLayoutUpdated() {
                component.getComponentTreeObserver().removeTreeLayoutChangedListener(this);
                int maxHeight = component.getHeight() * 5 / 8;
                int realHeight = listView.getHeight();
                ComponentContainer.LayoutConfig listParams = listView.getLayoutConfig();
                listParams.height = realHeight > maxHeight ? maxHeight : realHeight;
                listView.setLayoutConfig(listParams);
                DirectionalLayout.LayoutConfig marginParams = (DirectionalLayout.LayoutConfig) marginView.getLayoutConfig();
                marginParams.height = marginPx;
                marginView.setLayoutConfig(marginParams);
                enterAnimator();
            }

        });
        listView.setItemClickedListener(new ListContainer.ItemClickedListener() {
            @Override
            public void onItemClicked(ListContainer listContainer, Component component, int position, long l) {
                if (onItemClickListener != null) onItemClickListener.onItemClick(listContainer, component, position, l);
            }

        });
    }


    private void enterAnimator() {
        AnimatorProperty alpha = masker.createAnimatorProperty();
        try {
            Method alphaFrom = AnimatorProperty.class.getDeclaredMethod("alphaFrom", float.class);
            alphaFrom.setAccessible(true);
            alphaFrom.invoke(alpha, 0);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            alpha.alphaFrom(0);
        }
        alpha.alpha(1);
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(masker, "alpha", 0, 1);
        AnimatorProperty translationY = listView.createAnimatorProperty().moveFromY(listView.getHeight()).moveToY(0);
        AnimatorGroup group = new AnimatorGroup();
        group.setDuration(400);
        group.setCurveType(Animator.CurveType.ACCELERATE_DECELERATE);
        group.runParallel(alpha,translationY);
        group.start();
//        ObjectAnimator translationY = ObjectAnimator.ofFloat(listView, "translationY", listView.getHeight(), 0);
//        AnimatorSet set = new AnimatorSet();
//        set.setDuration(400);
//        set.playTogether(alpha, translationY);
//        set.setInterpolator(new AccelerateDecelerateInterpolator());
//        set.start();
    }


    @Override
    protected void onHide() {
        exitAnimator();
        super.onHide();
    }

    private void exitAnimator() {
//        AnimatorProperty  alpha = masker.createAnimatorProperty().alphaFrom(1).alpha(0);
        AnimatorProperty alpha = masker.createAnimatorProperty();
        try {
            Method alphaFrom = AnimatorProperty.class.getDeclaredMethod("alphaFrom", float.class);
            alphaFrom.setAccessible(true);
            alphaFrom.invoke(alpha, 1);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            alpha.alphaFrom(0);
        }
        alpha.alpha(0);
        AnimatorProperty translationY = listView.createAnimatorProperty().moveFromY(0).moveToY(listView.getHeight());
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(masker, "alpha", 1, 0);
//        ObjectAnimator translationY = ObjectAnimator.ofFloat(listView, "translationY", 0, listView.getHeight());
        AnimatorGroup group = new AnimatorGroup();
        group.setDuration(300);
        group.runParallel(alpha,translationY);
        group.setCurveType(Animator.CurveType.ACCELERATE_DECELERATE);
        group.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
                listView.setVisibility(Component.VISIBLE);
            }

            @Override
            public void onStop(Animator animator) {

            }

            @Override
            public void onCancel(Animator animator) {

            }

            @Override
            public void onEnd(Animator animator) {
                FolderPopUpWindow.super.hide();
            }

            @Override
            public void onPause(Animator animator) {

            }

            @Override
            public void onResume(Animator animator) {

            }
        });
        group.start();

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setSelection(int selection) {
        listView.setSelectedItemIndex(selection);
    }

    public void setMargin(int marginPx) {
        this.marginPx = marginPx;
    }

    @Override
    public void onClick(Component v) {
        hide();
    }

    @Override
    public boolean isTouchOutside() {
        return true;
    }

    public interface OnItemClickListener {
        void onItemClick(ListContainer listContainer, Component view, int position, long l);
    }
}
