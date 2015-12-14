package ws.arete.arms.engine.domain.geojson;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonGeometry {
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public List<Object> getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(ArrayList<Object> coordinates) {
        this.coordinates = coordinates;
    }

    private String type;
    private List<Object> coordinates;

    public GeoJsonGeometry(String type,List<Object> coordinates) {
        super();
        this.type = type;
        this.coordinates = coordinates;
    }
    
    public GeoJsonGeometry(String type, ArrayList<List> coordinateList) {
        super();
        this.type = type;
        this.coordinates = (List) coordinateList;
    }
}
