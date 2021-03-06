package com.changju.fanlitou.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.changju.fanlitou.R;
import com.changju.fanlitou.adapter.BannerAdapter;
import com.changju.fanlitou.base.WrapContentLinearLayoutManager;
import com.changju.fanlitou.bean.mine.AddBankInfo;
import com.changju.fanlitou.fragment.bank.BankAddFragment;
import com.changju.fanlitou.ui.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengww on 2017/6/5.
 */

public class AddBankInfoDialog extends DialogFragment implements View.OnClickListener {

    private NoScrollViewPager mViewPager;
    private TabLayout mTabLayout;

    private Activity context;

    private static AddBankInfo info;

    public static AddBankInfoDialog newInstance(AddBankInfo info) {

        AddBankInfoDialog.info = info;

        return new AddBankInfoDialog();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        Dialog dialog = new Dialog(getActivity(), R.style.BottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定

        dialog.setContentView(R.layout.dialog_add_bank_info);
        initView(dialog);

        // 设置宽度为屏宽, 靠近屏幕底部。
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.AnimBottom);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    private ProvinceAdapter mProvinceAdapter;
    List<AddBankInfo.ProvinceBean> mOneList;
    //一级tab选中
    int mCurrentOneSelect = -1;

    private CityAdapter mCityAdapter;
    List<AddBankInfo.ProvinceBean.CityBean> mTwoList;

    private void initView(Dialog dialog) {
        dialog.findViewById(R.id.iv_close).setOnClickListener(this);

        mTabLayout = (TabLayout) dialog.findViewById(
                R.id.tab_dialog_add_bank_info);
        mTabLayout.addOnTabSelectedListener(tabSelectedListener);

        mViewPager = (NoScrollViewPager) dialog.findViewById(
                R.id.view_pager_dialog_add_bank_info);

        RecyclerView oneView = new RecyclerView(context);
        oneView.setLayoutManager(new WrapContentLinearLayoutManager(context));
        RecyclerView twoView = new RecyclerView(context);
        twoView.setLayoutManager(new WrapContentLinearLayoutManager(context));

        List<View> recyclerViewList = new ArrayList<>();
        recyclerViewList.add(oneView);
        recyclerViewList.add(twoView);
        final BannerAdapter pagerAdapter = new BannerAdapter(
                recyclerViewList,new String[]{"请选择"," "});
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mOneList = info.getProvince();
        mProvinceAdapter = new ProvinceAdapter(mOneList);

        mProvinceAdapter.bindToRecyclerView(oneView);
        mProvinceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                AddBankInfo.ProvinceBean one = mOneList.get(position);

                mCurrentOneSelect = position;

                mTwoList = one.getCity();
                if (mTwoList == null || mTwoList.size() == 0) { // 选定一级。
                    ((BankAddFragment)getParentFragment()).
                            setResultFinish(one, null,null);
                    dismiss();
                } else {
                    // 更新二级的content和title。
                    mCityAdapter.setNewData(mTwoList);
                    mTabLayout.getTabAt(0).setText(one.getName());
                    mTabLayout.getTabAt(1).setText("请选择").select();
                    mViewPager.setCurrentItem(1);
                }
            }
        });

        mCityAdapter = new CityAdapter(null);
        mCityAdapter.bindToRecyclerView(twoView);
        mCityAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ((BankAddFragment)getParentFragment()).
                        setResultFinish(mOneList.get(mCurrentOneSelect),
                                mTwoList.get(position),null);
                dismiss();
            }
        });
        mCityAdapter.setEmptyView(R.layout.view_empty);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_close:
                dismiss();
                break;
        }

    }

    private class ProvinceAdapter extends BaseQuickAdapter<AddBankInfo.ProvinceBean, BaseViewHolder> {

        public ProvinceAdapter(List<AddBankInfo.ProvinceBean> data) {
            super(R.layout.recycler_item_add_bank_info, data);
        }

        @Override
        protected void convert(BaseViewHolder helper,
                               AddBankInfo.ProvinceBean item) {
            helper.setText(R.id.tv_content,item.getName());
        }
    }

    private class CityAdapter extends BaseQuickAdapter<AddBankInfo.ProvinceBean.CityBean,BaseViewHolder>{

        public CityAdapter( @Nullable List<AddBankInfo.ProvinceBean.CityBean> data) {
            super(R.layout.recycler_item_add_bank_info, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, AddBankInfo.ProvinceBean.CityBean item) {
            helper.setText(R.id.tv_content,item.getName());
        }
    }

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {

        private int mCurrentPosition;

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int newPosition = tab.getPosition();
            switch (newPosition) {
                case 1: {
                    if (mTwoList == null) {
                        mTabLayout.getTabAt(mCurrentPosition).select();
                        return;
                    }
                    break;
                }
            }
            this.mCurrentPosition = tab.getPosition();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };


}