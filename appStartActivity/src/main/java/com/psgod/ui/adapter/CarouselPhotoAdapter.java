package com.psgod.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.psgod.ui.fragment.CarouselPhotoDetailFragment;
import com.psgod.ui.fragment.PhotoDetailFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2015/11/26 0026.
 */
public class CarouselPhotoAdapter extends FragmentPagerAdapter {

    List<CarouselPhotoDetailFragment> fragments;

    public CarouselPhotoAdapter(FragmentManager fm , List<CarouselPhotoDetailFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}
