import java.util.HashSet;

public class MapNode {

    Long id;
    Long lon;
    Long lat;
    HashSet<String> nodes;
    String name;
    String wayType;
    String amenity;
    String address;


    public MapNode(Long id, Long lon, Long lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    public MapNode(Long id) {
        this.id = id;
    }

/*    public MapNode(Long id, Long lon, Long lat, String name, String amenity, String address) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.name = name;
        this.amenity = amenity;
        this.address = address;
    }

    public MapNode(Long id, HashSet nodes, String name, String wayType) {
        this.id = id;
        this.nodes = nodes;
        this.name = name;
        this.wayType = wayType;
    }*/

    public HashSet<String> getNodes() {
        return nodes;
    }

    public String getName() {
        return name;
    }

    public String getWayType() {
        return wayType;
    }

    public String getAmenity() {
        return amenity;
    }

    public String getAddress() {
        return address;
    }

    public void setNodes(HashSet<String> nodes) {
        this.nodes = nodes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWayType(String wayType) {
        this.wayType = wayType;
    }

    public void setAmenity(String amenity) {
        this.amenity = amenity;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
