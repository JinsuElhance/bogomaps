import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

/**
 *  Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 *  pathfinding, under some constraints.
 *  See OSM documentation on
 *  <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 *  and the java
 *  <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 *
 *  You may find the CSCourseGraphDB and CSCourseGraphDBHandler examples useful.
 *
 *  The idea here is that some external library is going to walk through the XML file, and your
 *  override method tells Java what to do every time it gets to the next element in the file. This
 *  is a very common but strange-when-you-first-see it pattern. It is similar to the Visitor pattern
 *  we discussed for graphs.
 *
 *  @author Alan Yao, Maurice Lee
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
    private String name;
    private String amenity;
    private String address;
    private HashSet<Long> tempRefs = new HashSet<Long>();
    private String highway;
    private Long id;
    private Long lon;
    private Long lat;

    /**
     * Create a new GraphBuildingHandler.
     * @param g The graph to populate with the XML data.
     */
    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    /**
     * Called at the beginning of an element. Typically, you will want to handle each element in
     * here, and you may want to track the parent element.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
     *            Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing
     *                  is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not
     *              available. This tells us which element we're looking at.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *                   shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @see Attributes
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            /* Encountering a new <node...> tag. */
            activeState = "node";
            // System.out.println("Node id: " + attributes.getValue("id"));
            // System.out.println("Node lon: " + attributes.getValue("lon"));
            // System.out.println("Node lat: " + attributes.getValue("lat"));
            id = Long.parseLong(attributes.getValue("id"));
            lat = Long.parseLong(attributes.getValue("lat"));
            lon = Long.parseLong(attributes.getValue("lon"));
            /* TODO: Use the above information to save a "node" to somewhere.
             * Hint: A graph-like structure would be nice. */

        } else if (qName.equals("way")) {
            id = Long.parseLong(attributes.getValue("id"));
            /* Encountering a new <way...> tag. */
            activeState = "way";
            // System.out.println("Beginning a way...");
        } else if (activeState.equals("way") && qName.equals("nd")) {
            tempRefs.add(Long.parseLong(attributes.getValue("ref")));

            /* While looking at a way, found a <nd...> tag. */
            // System.out.println("Node id in this way: " + attributes.getValue("ref"));

            /* TODO: Use the above id to make "possible" connections between the nodes in this way.
             * Hint 1: It would be useful to remember what was the last node in this way.
             * Hint 2: Not all ways are valid. So, directly connecting the nodes here would be
               cumbersome since you might have to remove the connections if you later see a tag that
               makes this way invalid. Instead, think of keeping a list of possible connections and
               remember whether this way is valid or not. */

        } else if (activeState.equals("way") && qName.equals("tag")) {
            /* While looking at a way, found a <tag...> tag. */
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                highway = v;

                // System.out.println("Highway type: " + v);

                /* TODO: Figure out whether this way and its connections are valid.
                //no
                 * Hint: Set a "flag". */

            } else if (k.equals("name")) {
                name = v;
                // System.out.println("Way Name: " + v);
            }
            // System.out.println("Tag with k=" + k + ", v=" + v + ".");
        } else if (activeState.equals("node") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("name")) {
                name = v;
            } else if (k.equals("amenity")) {
                amenity = v;
            } else if (k.equals("addr:street")) {
                address = v;
            }
            /* While looking at a node, found a <tag...> with k="name". */

            /* TODO: Create a location.
             * Hint: Since we found this <tag...> INSIDE a node, we should probably remember which
             * node this tag belongs to. Remember XML is parsed top-to-bottom, so probably it's the
             * last node that you looked at (check the first if-case). */

            // System.out.println("Node's name: " + attributes.getValue("v"));
        }
    }

    /**
     * Receive notification of the end of an element. You may want to take specific terminating
     * actions here, like finalizing vertices or edges found.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available.
     * @throws SAXException  Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way") && ALLOWED_HIGHWAY_TYPES.contains(highway)) {
            MapNode birthed = new MapNode(id, tempRefs, name, highway);
            g.add(birthed);
        } else if (qName.equals("node"))
            /* Done looking at a way. (Finished looking at the nodes, speeds, etc.) */

            /* Hint: If you have stored the possible connections for this way, here's your chance to
             * actually connect the nodes together if the way is valid. */

            // System.out.println("Finishing a way...");
        }
    }

}
