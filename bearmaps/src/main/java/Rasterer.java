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

        for (int depthcount = 0; depthcount <= MAX_DEPTH; depthcount ++) {
            if (0.08789062 / ((2^depthcount) * 256) <= queryDPP) {
                depth = depthcount;
            }
        }
        
        double tiledistance = 0.08789062 / (2^depth);

        int startX = (int) Math.ceil(((params.ullon - -122.29980468) / tiledistance) - 2);
        int endX = (int) Math.ceil((params.lrlon - params.ullon) / tiledistance);

        int startY = (int) Math.ceil(((37.89219554 - params.ullat) / tiledistance) - 2);
        int endY = (int) Math.ceil((params.ullat - params.lrlat) / tiledistance);

        String[][] result = new String[endY-startY][endX-startX];
        for (int currY = startY; currY <= endY; currY++) {
            for (int currX = startX; currX <= endX; currX++) {
                result[currY][currX] = "d" + depth + "_x" + currX + "_y" + currY + ".png";
            }
        }

        double RasterUllon = -122.29980468 + (tiledistance * startX);
        double Rasterlrlon = -122.29980468 + (tiledistance * endX);
        double RasterUllat = 37.89219554 - (tiledistance * startY);
        double Rasterlrlat = 37.89219554 - (tiledistance * endY);

        RasterResultParams.Builder toReturn = new RasterResultParams.Builder();
        toReturn = toReturn.setDepth(depth);
        toReturn = toReturn.setRasterLrLat(Rasterlrlat);
        toReturn = toReturn.setRasterLrLon(Rasterlrlon);
        toReturn = toReturn.setRasterUlLon(RasterUllon);
        toReturn = toReturn.setRasterUlLat(RasterUllat);
        toReturn = toReturn.setRenderGrid(result);
        toReturn = toReturn.setQuerySuccess(true);

        return toReturn.create();
    }

    private String[][] populateRender(int depth, int startX, int endX, int startY, int endY) {
        String[][] result = new String[endY-startY][endX-startX];
        for (int currY = startY; currY <= endY; currY++) {
            for (int currX = startX; currX <= endX; currX++) {
                result[currY][currX] = "d" + depth + "_x" + currX + "_y" + currY + ".png";
            }
        }
        return result;
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
