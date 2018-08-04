import java.util.ArrayList;

public class Way {

    String name;
    long id;
    String wayType;
    ArrayList<Long> nodes = new ArrayList<>();

    public Way(long id) {
        this.id = id;
    }

    public Way(String name, long id, String wayType) {
        this.name = name;
        this.id = id;
        this.wayType = wayType;
    }

    public ArrayList<Long> getNodes() {
        return nodes;
    }

    public String getWayType() {
        return wayType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWayType(String wayType) {
        this.wayType = wayType;
    }

    public void addConnection(long toAddId) {
        getNodes().add(toAddId);
    }
}
