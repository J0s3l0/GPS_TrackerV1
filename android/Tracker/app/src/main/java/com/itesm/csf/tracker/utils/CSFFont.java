package com.itesm.csf.tracker.utils;

import android.content.Context;
import android.graphics.Typeface;
import java.util.HashMap;

public class CSFFont {

    static private HashMap<String,Typeface> fonts;

    static public Typeface get(Context c, String font) {

        if (fonts == null) {
            fonts = new HashMap<>();
            fonts.put("fredoka", Typeface.createFromAsset(c.getAssets(), "fonts/FredokaOne.ttf"));
        }
        return fonts.get(font);
    }
}