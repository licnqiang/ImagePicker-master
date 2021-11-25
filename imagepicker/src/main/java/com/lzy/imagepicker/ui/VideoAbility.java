package com.lzy.imagepicker.ui;

import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.lzy.imagepicker.ResourceTable;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.ability.fraction.FractionAbility;
import ohos.aafwk.ability.fraction.FractionManager;
import ohos.aafwk.ability.fraction.FractionScheduler;
import ohos.aafwk.content.Intent;
import ohos.agp.components.ComponentContainer;
import ohos.agp.utils.Color;
import ohos.agp.window.service.WindowManager;
import ohos.utils.PlainArray;
import ohos.utils.PlainIntArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideoAbility extends FractionAbility {

    private BottomNavigationBar bottomNavigationViewEx;
    private FractionManager mFractionManager;
    private Fraction mCurFraction;
    public ComponentContainer rootView;

    private PlainIntArray items;// used for change ViewPager selected item
    private List<Fraction> fragments;// used for ViewPager adapter
    private PlainArray<Fraction> mFractionPlainArray = new PlainArray<>();

    @Override
    public void onStart(Intent intent) {
        WindowManager.getInstance().getTopWindow().get().setTransparent(true);
        super.setUIContent(ResourceTable.Layout_activity_main);
        initView();
        initData();
    }

    private void initView() {
        rootView = (ComponentContainer) findComponentById(ResourceTable.Id_rootView);
        bottomNavigationViewEx = (BottomNavigationBar) findComponentById(ResourceTable.Id_bottom_navigation);
        bottomNavigationViewEx.addItem(new BottomNavigationItem(ResourceTable.Media_grid_camera, "基础", getContext()))
//                .addItem(new BottomNavigationItem(ResourceTable.Media_api, "自定义", getContext()))
//                .addItem(new BottomNavigationItem(ResourceTable.Media_important, "列表", getContext()))
//                .addItem(new BottomNavigationItem(ResourceTable.Media_other, "更多", getContext()))
                .setFirstSelectedPosition(0).initialise();
        bottomNavigationViewEx.setInActiveColor(Color.GRAY.getValue());
        bottomNavigationViewEx.setBackgroundStyle(1);
        bottomNavigationViewEx.setBarMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationViewEx.setTabSelectedListener(navigationItemSelectedListener);
    }

    private void initData() {
        fragments = new ArrayList<>();
        items = new PlainIntArray();
        Fragment_1_Base basicsFragment = new Fragment_1_Base();
        basicsFragment.setDecorView(rootView);
//        Fragment_2_Custom customFragment = new Fragment_2_Custom();
//        customFragment.setDecorView(rootView);
//        Fragment_3_List complexFragment = new Fragment_3_List();
//        Fragment_4_More otherFragment = new Fragment_4_More();

        fragments.add(basicsFragment);
//        fragments.add(customFragment);
//        fragments.add(complexFragment);
//        fragments.add(otherFragment);
        mFractionManager = getFractionManager();
        createPageInContainer(ResourceTable.Id_stackLayout, 0);
    }

    private BottomNavigationBar.OnTabSelectedListener navigationItemSelectedListener = new BottomNavigationBar.OnTabSelectedListener() {

        @Override
        public void onTabSelected(int position) {
            createPageInContainer(ResourceTable.Id_stackLayout, position);
        }

        @Override
        public void onTabUnselected(int position) {

        }

        @Override
        public void onTabReselected(int i) {
        }
    };

    private void createPageInContainer(int container, int position) {
        // 开启事务
        FractionScheduler fractionScheduler = mFractionManager.startFractionScheduler();
        if (mCurFraction != null) {
            // 当前的fraction不为空，就隐藏
            fractionScheduler.hide(mCurFraction);
        }
        String tag = container + ":" + position;
        Fraction fraction;
        // 根据标签从FractionManager里面获取fraction
        Optional<Fraction> fractionOptional = mFractionManager.getFractionByTag(tag);
        if (fractionOptional.isPresent()) {
            fraction = fractionOptional.get();
            // 获取的fraction不为空，显示出来
            fractionScheduler.show(fraction);
        } else {
            // 获取的fraction为空，创建fraction
            fraction = getPage(position);
            // 将fraction添加到fractionScheduler
            fractionScheduler.add(container, fraction, tag);
        }
        mCurFraction = fraction;
        // 提交事务
        fractionScheduler.submit();
    }

    private Fraction getPage(int position) {
        // 从缓存获取fraction对象
        Optional<Fraction> fractionOptional = mFractionPlainArray.get(position);
        if (fractionOptional.isPresent()) {
            // 存在，直接返回
            return fractionOptional.get();
        }
        // 不存在fraction对象，则让子类通过反射创建fraction对象
        Fraction fraction = fragments.get(position);
        // 将创建好点的对象添加到缓存
        mFractionPlainArray.put(position, fraction);
        return fraction;
    }

//    @Override
//    protected void onActive() {
//        super.onActive();
////        Jzvd.goOnPlayOnResume();
//    }
//
//    @Override
//    protected void onInactive() {
//        super.onInactive();
//        Jzvd.goOnPlayOnPause();
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (backPress()) {
//            return;
//        }
//        super.onBackPressed();
//    }
//
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Jzvd.releaseAllVideos();
//        navigationItemSelectedListener = null;
//        bottomNavigationViewEx.setTabSelectedListener(null);
//    }
}
