package ws.arete.arms.engine.domain.geojson;

import java.util.List;

public class GeoJsonVehicleRoutingSolution {

    public List<Object> getFeatures() {
        return features;
    }
    public void setFeatures(List<Object> features) {
        this.features = features;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    protected List<Object> features;
    protected String type;

}
