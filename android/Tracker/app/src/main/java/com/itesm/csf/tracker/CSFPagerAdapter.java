package com.itesm.csf.tracker;

import android.support.v4.app.FragmentStatePagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class CSFPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public CSFPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new CSFHomeUser();
            case 1:
                return new CSFHomeUser();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
