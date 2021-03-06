import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Set;

/**
 * Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 * pathfinding, under some constraints.
 * See OSM documentation on
 * <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 * and the java
 * <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 * <p>
 * You may find the CSCourseGraphDB and CSCourseGraphDBHandler examples useful.
 * <p>
 * The idea here is that some external library is going to walk through the XML file, and your
 * override method tells Java what to do every time it gets to the next element in the file. This
 * is a very common but strange-when-you-first-see it pattern. It is similar to the Visitor pattern
 * we discussed for graphs.
 *
 * @author Alan Yao, Maurice Lee
 */
public class GraphBuildingHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = Set.of(
            "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified", "residential",
            "living_street", "motorway_link", "trunk_link", "primary_link", "secondary_link",
            "tertiary_link"
    );
    private String activeState = "";
    private final GraphDB g;
    private MapNode lastNode;
    private Way lastWay;

    /**
     * Create a new GraphBuildingHandler.
     *
     * @param g The graph to populate with the XML data.
     */
    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        if (qName.equals("node")) {
            activeState = "node";
            Long id = Long.parseLong(attributes.getValue("id"));
            Double lon = Double.parseDouble(attributes.getValue("lon"));
            Double lat = Double.parseDouble(attributes.getValue("lat"));
            lastNode = new MapNode(id, lon, lat);

        } else if (qName.equals("way")) {
            Long id = Long.parseLong(attributes.getValue("id"));
            lastWay = new Way(id);
            activeState = "way";

        } else if (activeState.equals("way") && qName.equals("nd")) {
            lastWay.addConnection(Long.parseLong(attributes.getValue("ref")));

        } else if (activeState.equals("way") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                lastWay.setWayType(v);

            } else if (k.equals("name")) {
                lastWay.setName(v);
            }

        } else if (activeState.equals("node") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("name")) {
                lastNode.setName(v);
            } else if (k.equals("amenity")) {
                lastNode.setAmenity(v);
            } else if (k.equals("addr:street")) {
                lastNode.setAddress(v);
            }

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equals("way") && lastWay != null && lastWay.getWayType() != null
                && ALLOWED_HIGHWAY_TYPES.contains(lastWay.getWayType())) {
            g.add(lastWay);
            activeState = "";
            lastWay = null;
        } else if (qName.equals("node") && !(lastNode == null)) {
            g.add(lastNode);
            activeState = "";
            lastNode = null;
        }


    }
}
