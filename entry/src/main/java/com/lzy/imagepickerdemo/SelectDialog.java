package com.lzy.imagepickerdemo;

import com.lzy.imagepicker.util.ResUtil;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.*;
import ohos.agp.components.element.ElementScatter;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.service.DisplayManager;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;

import java.util.List;

/**
 * 选择对话框
 * <p>
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2017-03-22  11:38
 */

public class SelectDialog extends CommonDialog implements Component.ClickedListener, ListContainer.ItemClickedListener {
    private SelectDialogListener mListener;
    private Ability ability;
    private Button mMBtn_Cancel;
    private Text mTv_Title;
    private List<String> mName;
    private String mTitle;
    private boolean mUseCustomColor = false;
    private int mFirstItemColor;
    private int mOtherItemColor;
    Component root;

    @Override
    public void onItemClicked(ListContainer listContainer, Component component, int position, long id) {
        mListener.onItemClick(listContainer, component, position, id);
        hide();
    }

    public interface SelectDialogListener {
        public void onItemClick(ListContainer parent, Component view, int position, long id);
    }


    /**
     * 取消事件监听接口
     */
    private SelectDialogCancelListener mCancelListener;

    public interface SelectDialogCancelListener {
        public void onCancelClick(Component v);
    }

    public SelectDialog(Ability ability, int theme,
                        SelectDialogListener listener, List<String> names) {
        super(ability);
        this.ability = ability;
        mListener = listener;
        this.mName = names;

        setAutoClosable(true);
    }

    /**
     * @param ability        调用弹出菜单的ability
     * @param theme          主题
     * @param listener       菜单项单击事件
     * @param cancelListener 取消事件
     * @param names          菜单项名称
     */
    public SelectDialog(Ability ability, int theme, SelectDialogListener listener, SelectDialogCancelListener cancelListener, List<String> names) {
        super(ability);
        this.ability = ability;
        mListener = listener;
        mCancelListener = cancelListener;
        this.mName = names;

        // 设置是否点击外围不解散
        setAutoClosable(false);
    }

    /**
     * @param ability  调用弹出菜单的ability
     * @param theme    主题
     * @param listener 菜单项单击事件
     * @param names    菜单项名称
     * @param title    菜单标题文字
     */
    public SelectDialog(Ability ability, int theme, SelectDialogListener listener, List<String> names, String title) {
        super(ability);
        this.ability = ability;
        mListener = listener;
        this.mName = names;
        mTitle = title;

        // 设置是否点击外围可解散
        setAutoClosable(true);
    }

    public SelectDialog(Ability ability, int theme, SelectDialogListener listener, SelectDialogCancelListener cancelListener, List<String> names, String title) {
        super(ability);
        this.ability = ability;
        mListener = listener;
        mCancelListener = cancelListener;
        this.mName = names;
        mTitle = title;

        // 设置是否点击外围可解散
        setAutoClosable(true);
    }


    @Override
    protected void onCreate() {
        super.onCreate();
        root = LayoutScatter.getInstance(ability).parse(ResourceTable.Layout_view_dialog_select,
                null, false);
        setContentCustomComponent(root);
//        setContentComponent(view, new LayoutParams(LayoutParams.FILL_PARENT,
//                LayoutParams.match_content));
        Window window = getWindow();

        // 设置显示动画

//        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutConfig wl = window.getLayoutConfig().get();
        wl.x = 0;
        wl.y = DisplayManager.getInstance().getDefaultDisplay(ability).get().getAttributes().height;
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ComponentContainer.LayoutConfig.MATCH_PARENT;
        wl.height = ComponentContainer.LayoutConfig.MATCH_CONTENT;

        // 设置显示位置
//        onWindowAttributesChanged(wl);
        onWindowConfigUpdated(wl);
        initComponents();
    }


    private void initComponents() {
        DialogAdapter dialogAdapter = new DialogAdapter(mName);
        ListContainer dialogList = (ListContainer) root.findComponentById(ResourceTable.Id_dialog_list);
        dialogList.setItemClickedListener(this);
        dialogList.setItemProvider(dialogAdapter);
        mMBtn_Cancel = (Button) root.findComponentById(ResourceTable.Id_mBtn_Cancel);
        mTv_Title = (Text) root.findComponentById(ResourceTable.Id_mTv_Title);


        mMBtn_Cancel.setClickedListener(new Component.ClickedListener() {

            @Override
            public void onClick(Component v) {
                // TODO Auto-generated method stub
                if (mCancelListener != null) {
                    mCancelListener.onCancelClick(v);
                }
                hide();
            }
        });

        if (!(mTitle == null || mTitle.equals("")) && mTv_Title != null) {
            mTv_Title.setVisibility(Component.VISIBLE);
            mTv_Title.setText(mTitle);
        } else {
            mTv_Title.setVisibility(Component.HIDE);
        }
    }

    @Override
    public void onClick(Component v) {
        hide();

    }


    private class DialogAdapter extends BaseItemProvider {
        private List<String> mStrings;
        private Componentholder viewholder;
        private LayoutScatter layoutScatter;

        public DialogAdapter(List<String> strings) {
            this.mStrings = strings;
            this.layoutScatter = LayoutScatter.getInstance(ability);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mStrings.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mStrings.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public Component getComponent(int position, Component convertComponent, ComponentContainer componentContainer) {
            if (null == convertComponent) {
                viewholder = new Componentholder();
                convertComponent = layoutScatter.parse(ResourceTable.Layout_view_dialog_item, null, false);
                viewholder.dialogItemButton = (Text) convertComponent.findComponentById(ResourceTable.Id_dialog_item_bt);
                convertComponent.setTag(viewholder);
            } else {
                viewholder = (Componentholder) convertComponent.getTag();
            }
            viewholder.dialogItemButton.setText(mStrings.get(position));
            if (!mUseCustomColor) {
                mFirstItemColor = ResUtil.getColor(ability, ResourceTable.Color_blue);
                mOtherItemColor = ResUtil.getColor(ability, ResourceTable.Color_blue);
            }
            ElementScatter scatter = ElementScatter.getInstance(ability);
            if (1 == mStrings.size()) {
                viewholder.dialogItemButton.setTextColor(new Color(mFirstItemColor));
                viewholder.dialogItemButton.setBackground(scatter.parse(ResourceTable.Graphic_dialog_item_bg_only));
            } else if (position == 0) {
                viewholder.dialogItemButton.setTextColor(new Color(mFirstItemColor));
                viewholder.dialogItemButton.setBackground(scatter.parse(ResourceTable.Graphic_select_dialog_item_bg_top));
            } else if (position == mStrings.size() - 1) {
                viewholder.dialogItemButton.setTextColor(new Color(mOtherItemColor));
                viewholder.dialogItemButton.setBackground(scatter.parse(ResourceTable.Graphic_select_dialog_item_bg_buttom));
            } else {
                viewholder.dialogItemButton.setTextColor(new Color(mOtherItemColor));
                viewholder.dialogItemButton.setBackground(scatter.parse(ResourceTable.Graphic_select_dialog_item_bg_center));
            }
            return convertComponent;
        }

    }

    public static class Componentholder {
        public Text dialogItemButton;
    }

    /**
     * 设置列表项的文本颜色
     * @param firstItemColor 第一个项目颜色
     * @param otherItemColor 其他项目颜色
     */
    public void setItemColor(int firstItemColor, int otherItemColor) {
        mFirstItemColor = firstItemColor;
        mOtherItemColor = otherItemColor;
        mUseCustomColor = true;
    }
}
