import java.util.HashSet;

public class MapNode implements Comparable<MapNode> {

    Long id;
    Double lon;
    Double lat;
    String name;
    String amenity;
    String address;

    public MapNode(Long id, Double lon, Double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    public MapNode(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }


    public String getAmenity() {
        return amenity;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmenity(String amenity) {
        this.amenity = amenity;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int compareTo(MapNode other) {

    }
}
