package com.psgod.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.psgod.R;
import com.psgod.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CarouselPhotoDetailFragment extends Fragment {


    public CarouselPhotoDetailFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_carousel_photo_detail, null);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = Utils.dpToPx(getActivity(),280);
        params.height = Utils.dpToPx(getActivity(),280);
        view.setLayoutParams(params);
        return view;
    }


}
