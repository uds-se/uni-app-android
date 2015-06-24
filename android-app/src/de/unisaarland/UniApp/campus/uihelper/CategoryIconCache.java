package de.unisaarland.UniApp.campus.uihelper;


import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CategoryIconCache {

    private static final String TAG = CategoryIconCache.class.getSimpleName();

    /* Maps to null if the icon does not exist */
    private final Map<Integer, Drawable> cachedIcons = new HashMap<Integer, Drawable>();

    private final AssetManager assets;

    public CategoryIconCache(AssetManager assets) {
        this.assets = assets;
    }

    public synchronized Drawable getIconForCategory(int categoryID) {
        Drawable d = cachedIcons.get(categoryID);

        if (d == null) {
            String imageFile = "cat" + categoryID + ".png";
            try {
                d = Drawable.createFromStream(assets.open(imageFile), imageFile);
            } catch (IOException e) {
                Log.e(TAG, "Cannot open asset '" + imageFile + "'", e);
            }
            cachedIcons.put(categoryID, d); // yes, also if null
        }

        return d;
    }

}
