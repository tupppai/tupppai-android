package com.psgod.ui.widget;

/**
 * 城市选择器
 *
 * @author brandwang
 */

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.ui.adapter.CityInfo;
import com.psgod.ui.adapter.CountryAdapter;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

public class CitySelector {
    private static Context cx;
    private static final int CONFIRM_CHOOSE_PLACE = 1001;

    // Scrolling 标志位
    private static boolean scrolling = false;
    private static TextView tv;
    private static Handler mHandler;
    private static Button button_ok;
    private static Button button_cancel;
    private static TextView mSetPlacetText;
    static CountryAdapter adapter;

    // 创建一个包含自定义view的PopupWindow
    public static PopupWindow makePopupWindow(Context context, Handler handler) {
        cx = context;
        mHandler = handler;

        final PopupWindow window;
        window = new PopupWindow(cx);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        window.setBackgroundDrawable(dw);

        View contentView = LayoutInflater.from(cx).inflate(
                R.layout.widget_city_selector, null);
        window.setContentView(contentView);

        // 即时城市显示
        tv = (TextView) contentView.findViewById(R.id.tv_cityName);

        // 省选择器
        final WheelView country = (WheelView) contentView
                .findViewById(R.id.country);
        country.setVisibleItems(5);

        adapter = new CountryAdapter(cx);
        country.setViewAdapter(adapter);

        // final String cities[][] = AddressData.CITIES;
        final String cities[][] = CityInfo.getCityName();

        // 市选择器
        final WheelView city = (WheelView) contentView.findViewById(R.id.city);
        city.setVisibleItems(5);

        // 省选择器监听器
        country.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    updateCities(city, cities, newValue);
                }
            }
        });

        country.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                updateCities(city, cities, country.getCurrentItem());

                try {
                    tv.setText(CityInfo.getProvinceName()[country.getCurrentItem()]
                            + "-"
                            + CityInfo.getCityName()[country.getCurrentItem()][city
                            .getCurrentItem()]);
                } catch (Exception e) {
                }
            }
        });

        city.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;

                try {
                    tv.setText(CityInfo.getProvinceName()[country.getCurrentItem()]
                            + "-"
                            + CityInfo.getCityName()[country.getCurrentItem()][city
                            .getCurrentItem()]);
                } catch (Exception e) {
                }

            }
        });

        country.setCurrentItem(1);

        // 点击事件处理
        button_ok = (Button) contentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer[] provinceId = CityInfo.getProvinceId();
                Integer[][] cityId = CityInfo.getCityId();
                String[] provinceName = CityInfo.getProvinceName();
                String[][] cityName = CityInfo.getCityName();

                Message msg = mHandler.obtainMessage();
                msg.what = CONFIRM_CHOOSE_PLACE;
                // bundle 传递参数
                Bundle bundle = new Bundle();
                // id 和 position不是一个东西
                bundle.putInt("provinceId",
                        provinceId[country.getCurrentItem()]);
                bundle.putInt("cityId",
                        cityId[country.getCurrentItem()][city.getCurrentItem()]);
                bundle.putString("provinceName",
                        provinceName[country.getCurrentItem()]);
                bundle.putString("cityName",
                        cityName[country.getCurrentItem()][city
                                .getCurrentItem()]);

                msg.setData(bundle);
                msg.sendToTarget();

                window.dismiss();
            }
        });

        button_cancel = (Button) contentView.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });

        window.setWidth(Constants.WIDTH_OF_SCREEN);
        window.setHeight(Constants.HEIGHT_OF_SCREEN / 3);

        // 设置PopupWindow外部区域是否可触摸
        window.setFocusable(true); // 设置PopupWindow可获得焦点
        window.setTouchable(true); // 设置PopupWindow可触摸
        window.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸

        return window;
    }

    /**
     * Updates the city wheel
     */
    private static void updateCities(WheelView city, String cities[][],
                                     int index) {
        ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(cx,
                cities[index]);

        city.setViewAdapter(adapter);
        city.setCurrentItem(cities[index].length / 2);
    }
}
