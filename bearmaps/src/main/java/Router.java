import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     *
     * @param g       <code>GraphDB</code> data source.
     * @param stlon   The longitude of the starting coordinate.
     * @param stlat   The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */

    //Compare nodes in the fringe using a comparator that pulls the
    //distance between two nodes from the bestDist hashMap
    public static List<Long> shortestPath(GraphDB g,
                                   double stlon, double stlat,
                                   double destlon, double destlat) {

        HashMap<Long, Double> bestDist = new HashMap<>();
        HashSet<Long> visited = new HashSet<>();
        HashMap<Long, Long> paths = new HashMap<>();

        long sourceNode = g.closest(stlon, stlat);
        long destNode = g.closest(destlon, destlat);
        PriorityQueue<Long> fringe = new PriorityQueue<>((o1, o2)
            -> Double.compare((bestDist.get(o1)
                + g.distance(o1, sourceNode)), bestDist.get(o2) + g.distance(o2, sourceNode)));

        fringe.add(sourceNode);
        visited.add(sourceNode);
        paths.put(sourceNode, sourceNode);
        bestDist.put(sourceNode, 0.0);

        while (!fringe.isEmpty()) {

            Long checkNode = fringe.poll();

            while (visited.contains(checkNode) && !fringe.isEmpty()) {
                checkNode = fringe.poll();
            }

            if (checkNode.equals(destNode)) {
                break;
            }

            visited.add(checkNode);

            for (Long w : g.adjacent(checkNode)) {

                if (!bestDist.containsKey(w)) {
                    bestDist.put(w, 1E99);
                }

                if (bestDist.get(checkNode) + g.distance(checkNode, w) < bestDist.get(w)) {

                    bestDist.put(w, bestDist.get(checkNode) + g.distance(checkNode, w));
                    fringe.remove(w);
                    fringe.add(w);

                    paths.put(w, checkNode);
                }
            }
        }
        return pathFinder(paths, sourceNode, destNode);
    }

    public static List<Long> pathFinder(HashMap paths, Long sourceNode, Long finalNode) {
        Long current = (Long) paths.get(finalNode);
        ArrayList<Long> result = new ArrayList<>();
        result.add(0, finalNode);
        while (!current.equals(sourceNode)) {
            result.add(0, current);
            current = (Long) paths.get(current);
        }
        result.add(0, sourceNode);
        return result;
    }

    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     * @param g <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        // TODO
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction represented.*/
        int direction;
        /** The name of this way. */
        String way;
        /** The distance along this way. */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }

    /** Radius of the Earth in miles. */
    private static final int R = 3963;

    public static double distance(double stlon, double stlat, double destlon, double destlat) {
        double phi1 = Math.toRadians(destlat);
        double phi2 = Math.toRadians(stlat);
        double dphi = Math.toRadians(stlat - destlat);
        double dlambda = Math.toRadians(stlon - destlon);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
