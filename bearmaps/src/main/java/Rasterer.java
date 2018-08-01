import java.util.HashSet;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /** The max image depth level. */
    public static final int MAX_DEPTH = 7;

    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     *     possible, while still covering less than or equal to the amount of longitudinal distance
     *     per pixel in the query box for the user viewport size.</li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the above
     *     condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    public RasterResultParams getMapRaster(RasterRequestParams params) {
        if (params.ullon > params.lrlon || params.lrlat > params.ullat) {
            return RasterResultParams.queryFailed();
        }

        double queryDPP = lonDPP(params.lrlon, params.ullon, params.w);
        int depth = 0;

        for (int depthcount = 0; depthcount <= MAX_DEPTH; depthcount++) {
            depth = depthcount;
            if (MapServer.ROOT_LON_DELTA / ((Math.pow(2, depthcount)) * 256) <= queryDPP) {
                break;
            }
        }

        double tileLon = MapServer.ROOT_LON_DELTA / Math.pow(2, depth);
        double tileLat = MapServer.ROOT_LAT_DELTA / Math.pow(2, depth);
        //I think we should get 9 here.
        int startX = (int) Math.floor((params.ullon - MapServer.ROOT_ULLON) / tileLon);
        int endX = (int) Math.ceil((params.lrlon - MapServer.ROOT_ULLON) / tileLon);
        //I think we should get 4 here if our tileDistanceLat is wrong.
        int startY = (int) Math.floor((MapServer.ROOT_ULLAT - params.ullat) / tileLat);
        int endY = (int) Math.ceil((MapServer.ROOT_ULLAT - params.lrlat) / tileLat);

        double RasterUllon = MapServer.ROOT_ULLON + (tileLon * startX);
        double RasterLrlon = MapServer.ROOT_ULLON + (tileLon * endX);
        double RasterUllat = MapServer.ROOT_ULLAT - (tileLat * startY);
        double RasterLrlat = MapServer.ROOT_ULLAT - (tileLat * endY);

        String[][] result = new String[(endY - startY)][(endX - startX)];

        if (depth == 0) {
            result[0][0] = "d0_x0_y0.png";
        } else {
            for (int currY = startY; currY < endY; currY++) {
                for (int currX = startX; currX < endX; currX++) {
                    result[currY - startY][currX - startX] = "d" + depth + "_x" + currX + "_y" + currY + ".png";
                }
            }
        }

        RasterResultParams.Builder toReturn = new RasterResultParams.Builder();
        toReturn = toReturn.setDepth(depth);
        toReturn = toReturn.setRasterLrLat(RasterLrlat);
        toReturn = toReturn.setRasterLrLon(RasterLrlon);
        toReturn = toReturn.setRasterUlLon(RasterUllon);
        toReturn = toReturn.setRasterUlLat(RasterUllat);
        toReturn = toReturn.setRenderGrid(result);
        toReturn = toReturn.setQuerySuccess(true);

        return toReturn.create();
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }
}
