import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 */
public class GraphDB {

    private KDTree<ProxNode> theProximityMap;
    private final Map<Long, ArrayList<Edge>> edgeConnections = new HashMap<>();
    private final Map<Long, MapNode> nodes = new HashMap<>();
    private boolean constructed = false;
    private ArrayList<ProxNode> KDNodes;

    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        File inputFile = new File(dbPath);
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    public void add(MapNode toPlace) {
        nodes.put(toPlace.id, toPlace);
        edgeConnections.put(toPlace.id, new ArrayList<>());
    }

    public void add(Way newWay) {

        ArrayList<Long> listOf = new ArrayList<>(newWay.nodes);

        if (listOf.size() > 1) {
            int curr = 0;
            int next = 1;
            while (next < listOf.size()) {
                MapNode nodeOne = nodes.get(listOf.get(curr));
                MapNode nodeTwo = nodes.get(listOf.get(next));
                edgeConnections.get(nodeOne.id).add(new Edge(nodeOne, nodeTwo, newWay.id));
                edgeConnections.get(nodeTwo.id).add(new Edge(nodeOne, nodeTwo, newWay.id));
                curr++;
                next++;
            }
        } else {
            return;
        }
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }
    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> ids = new ArrayList<>(edgeConnections.keySet());

        for (Long nodeId : ids) {
            if (edgeConnections.get(nodeId).size() == 0) {
                edgeConnections.remove(nodeId);
                nodes.remove(nodeId);
            }
        }
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {
        if (nodes.containsKey(v)) {
            return nodes.get(v).lon;
        }
        return 0.0;
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {
        if (nodes.containsKey(v)) {
            return nodes.get(v).lat;
        }
        return 0.0;
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        if (edgeConnections.containsKey(v)) {
            List<Long> result = new ArrayList<Long>();
            for (Edge edge : edgeConnections.get(v)) {
                if (edge.oneEnd.id == v) {
                    result.add(edge.twoEnd.id);
                } else {
                    result.add(edge.oneEnd.id);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double distance(double longitude, double latitude, long v) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(latitude);
        double dphi = Math.toRadians(latitude - lat(v));
        double dlambda = Math.toRadians(longitude - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Returns the ID of the vertex closest to the given longitude and latitude.
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    public long closest(double lon, double lat) {
        double smallestDist = 1E99;
        long smallestId = -1;
        for (long checkId : nodes.keySet()) {
            double currDist = distance(lon, lat, checkId);
            if (currDist < smallestDist) {
                smallestDist = currDist;
                smallestId = checkId;
            }
        }
        return smallestId;
    }

    public long kdClosest(double lon, double lat) {

        //Constructs the kdTree
        if (!constructed) {
            for (Long id : nodes.keySet()) {
                KDNodes.add(new ProxNode(id));
            }
            theProximityMap = kdConstruct(KDNodes, true);
            constructed = true;
        }

        //Indexes into the kdTree already created
        boolean checkX = true;
        double queryX = projectToX(lon, lat);
        double queryY = projectToY(lon, lat);
        return kdClosestHelper(null, 1E99, 1E99, queryX, queryY, checkX, theProximityMap);
    }

    public Long kdClosestHelper(Long currentBest, double currbestX, double currbestY, double queryX, double queryY, boolean checkX, KDTree<ProxNode> t) {
        //Base Case + Check only right, check only left.
        if (t.left == null && t.right == null) {
            return t.item.id;
        } else if (t.right == null && !(t.left == null)) {
            return kdClosestHelper(currentBest, currbestX, currbestY, queryX, queryY, !checkX, t.right);
        } else if (t.left == null && !(t.right == null)) {
            return kdClosestHelper(currentBest, currbestX, currbestY, queryX, queryY, !checkX, t.left);
        }

        //If the current node is closer than the current best, then it becomes the current best.
        if (euclidean(t.item.x, t.item.y, queryY, queryX) < euclidean(currbestX, currbestY, queryX, queryY)) {
            currentBest = t.item.id;
        }

        if (checkX && Math.abs(queryX - t.item.x) < euclidean(queryX, queryY, currbestX, currbestY)) {
//            If the hypersphere (wtf is this)  crosses the plane, there could be nearer points on the other side
//                of the plane, so the algorithm must move down the other branch of the tree from the current node looking
//                for closer points, following the same recursive process as the entire search.
//            If the hypersphere doesn't intersect the splitting plane, then the algorithm continues walking up the tree,
//              and the entire branch on the other side of that node is eliminated.
        }



        if (checkX && queryX < t.item.x) {
            //recurse on t.right with checkX = false;
        } else if (checkX && queryX > t.item.x) {
            //recurse on t.left with checkX = false;
        } else if (!checkX && queryY < t.item.y) {
            //recurse on t.right with with checkX = true;
        } else if (!checkX && queryY > t.item.y) {
            //recurse on t.left with with checkX = true;
        }


    }


    public KDTree<ProxNode> kdConstruct(ArrayList<ProxNode> kdNodes, boolean sortByX) {
        if (kdNodes.size() == 1) {
            return new KDTree<ProxNode>(kdNodes.get(0));
        } else {
            if (sortByX) {
                kdNodes.sort((o1, o2) -> Double.compare(o1.x, o2.x));
            } else {
                kdNodes.sort((o1, o2) -> Double.compare(o1.y, o2.y));
            }
            ProxNode median = kdNodes.get(kdNodes.size() / 2);
            KDTree<ProxNode> toReturn = new KDTree<ProxNode>(median);
            toReturn.left = kdConstruct(new ArrayList<ProxNode>(kdNodes.subList(0, kdNodes.size() / 2)), !sortByX);
            if (kdNodes.size() != 2) {
                toReturn.right = kdConstruct(new ArrayList<ProxNode>(kdNodes.subList(kdNodes.size() / 2 + 1, kdNodes.size())), !sortByX);
            }
            return toReturn;
        }
    }

    static double euclidean(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {
        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /** Radius of the Earth in miles. */
    private static final int R = 3963;
    /** Latitude centered on Berkeley. */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /** Longitude centered on Berkeley. */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;

    public class Edge implements Comparable<Edge>{

        long wayId;
        double distance;
        MapNode oneEnd;
        MapNode twoEnd;

        public Edge(MapNode oneEnd, MapNode twoEnd, long wayId) {
            this.oneEnd = oneEnd;
            this.twoEnd = twoEnd;
            this.wayId = wayId;
            this.distance = distance(oneEnd.id, twoEnd.id);
        }

        public int compareTo(Edge other) {
            double distance1 = distance(this.oneEnd.id, this.twoEnd.id);
            double distance2 = distance(other.oneEnd.id, other.twoEnd.id);
            if (distance1 == distance2) {
                return 0;
            } else if (distance1 > distance2) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public class ProxNode {
        long id;
        double x;
        double y;

        public ProxNode(long id) {
            MapNode newNode = nodes.get(id);
            this.x = projectToX(newNode.lon, newNode.lat);
            this.y = projectToY(newNode.lon, newNode.lat);
        }
    }
}
