package com.pires.wesee.model;

import java.util.List;

/**
 * Created by pires on 15/12/28.
 */
public class SelectFolder {
    public String name;
    public String path;
    public SelectImage cover;
    public List<SelectImage> images;

    @Override
    public boolean equals(Object o) {
        try {
            SelectFolder other = (SelectFolder) o;
            return this.path.equalsIgnoreCase(other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
