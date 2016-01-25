package de.unisaarland.UniApp.campus.model;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class CustomMapTileProvider implements TileProvider {

    public static class Hom extends CustomMapTileProvider {

        public Hom(AssetManager assets) {
            super(assets, "tiles/hom/OverlayTiles");
        }

        @Override
        public final SparseArray<Rect> getTileZooms() {
            return new SparseArray<Rect>() {{
                put(14,  new Rect(8525,  5604,  8526,  5605 ));
                put(15,  new Rect(17051,  11209,  17053,  11210 ));
                put(16, new Rect(34103,  22419,  34106,  22421 ));
                put(17, new Rect(68207, 44839, 68212, 44842));
            }};
        }

    }

    public static class Saar extends CustomMapTileProvider {

        public Saar(AssetManager assets) {
            super(assets, "tiles/saar/OverlayTiles");
        }

        @Override
        public final SparseArray<Rect> getTileZooms() {
            return new SparseArray<Rect>() {{
                put(14,  new Rect(8512,  10774,  8512,  10775 ));
                put(15,  new Rect(107024,  21549,  107025,  21550 ));
                put(16, new Rect(34048,  43099,  34051,  43101 ));
                put(17, new Rect(68097, 86198, 68102, 86203));
                put(18, new Rect(136194, 172396, 136205, 172406));
            }};
        }

    }

    public static class Dudweiler extends CustomMapTileProvider {

        public Dudweiler(AssetManager assets) {
            super(assets, "tiles/saar/DudweilerTiles");
        }

        @Override
        public final SparseArray<Rect> getTileZooms() {
            return new SparseArray<Rect>() {{
                put(14,  new Rect(8512,  10776,  8512,  10776 ));
                put(15,  new Rect(17024,  21552,  17025,  21553 ));
                put(16, new Rect(34048,  43105,  34050,  43106 ));
                put(17, new Rect(68096, 86210, 68100, 86213));
                put(18, new Rect(136193, 172420, 136200, 172426));
                put(19, new Rect(272387, 344841, 272401, 344852));
            }};
        }

    }


    public static class SaarSport extends CustomMapTileProvider {

        public SaarSport(AssetManager assets) {
            super(assets, "tiles/saar/OverlayTilesSport");
        }

        @Override
        public final SparseArray<Rect> getTileZooms() {
            return new SparseArray<Rect>() {{
                put(14,  new Rect(135,  180,  135,  181 ));
                put(15,  new Rect(270,  361,  271,  363 ));
                put(16, new Rect(541,  723,  543,  726 ));
                put(17, new Rect(1082, 1447, 1086, 1452));
                put(18, new Rect(2165, 2894, 2172, 2905));
                put(19, new Rect(2165, 2894, 2172, 2905));
            }};
        }

    }

    public static CustomMapTileProvider[] allTileProviders(AssetManager assets) {
        return new CustomMapTileProvider[] {
            new Saar(assets),
            new Dudweiler(assets),
            new SaarSport(assets),
            new Hom(assets)
        };
    }

    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;

    private final String tilesFolder;

    private final AssetManager mAssets;

    public CustomMapTileProvider(AssetManager assets, String tilesFolder) {
        mAssets = assets;
        this.tilesFolder = tilesFolder;
    }

    protected abstract SparseArray<Rect> getTileZooms();

    private boolean hasTile(int x, int y, int zoom) {
        Rect b = getTileZooms().get(zoom);
        return b != null;
//        (b.left <= x && x <= b.right && b.top <= y && y <= b.bottom);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        y = fixYCoordinate(y, zoom);
        if (!hasTile(x,y,zoom))
            return NO_TILE;
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {
            try {
                in = mAssets.open(getTileFilename(x, y, zoom));
            } catch (FileNotFoundException e) {
                return null;
            }
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            AssertionError err = new AssertionError("error reading tile: " + e);
            err.setStackTrace(e.getStackTrace());
            throw err;
        }
    }

    private String getTileFilename(int x, int y, int zoom) {
        return tilesFolder + '/' + zoom + '/' + x + '/' + y + ".png";
    }

    /**
     * Fixing tile's y index (reversing order)
     */
    private int fixYCoordinate(int y, int zoom) {
        int size = 1 << zoom; // size = 2^zoom
        return size - 1 - y;
    }
}
