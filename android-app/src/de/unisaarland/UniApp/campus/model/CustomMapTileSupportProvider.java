package de.unisaarland.UniApp.campus.model;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.SparseArray;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/14/13
 * Time: 2:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomMapTileSupportProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;

    private AssetManager mAssets;

    public CustomMapTileSupportProvider(AssetManager assets) {
        mAssets = assets;
    }

    private static final SparseArray<Rect> TILE_ZOOMS = new SparseArray<Rect>() {{
        put(14,  new Rect(135,  180,  135,  181 ));
        put(15,  new Rect(270,  361,  271,  363 ));
        put(16, new Rect(541,  723,  543,  726 ));
        put(17, new Rect(1082, 1447, 1086, 1452));
        put(18, new Rect(2165, 2894, 2172, 2905));
        put(19, new Rect(2165, 2894, 2172, 2905));
    }};

    private boolean hasTile(int x, int y, int zoom) {
        Rect b = TILE_ZOOMS.get(zoom);
        return b == null ? false : true;
        //TODO: both places add check
        //(b.left <= x && x <= b.right && b.top <= y && y <= b.bottom);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        y = fixYCoordinate(y, zoom);
        if(hasTile(x,y,zoom)){
            byte[] image = readTileImage(x, y, zoom);
            return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
        }
        return NO_TILE;
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {
            in = mAssets.open(getTileFilename(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    private String getTileFilename(int x, int y, int zoom) {
        return "OverlayTilesSport/" + zoom + '/' + x + '/' + y + ".png";
    }

    /**
     * Fixing tile's y index (reversing order)
     */
    private int fixYCoordinate(int y, int zoom) {
        int size = 1 << zoom; // size = 2^zoom
        return size - 1 - y;
    }
}
