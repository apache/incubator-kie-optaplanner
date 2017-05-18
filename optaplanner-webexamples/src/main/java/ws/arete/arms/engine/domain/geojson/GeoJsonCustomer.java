package ws.arete.arms.engine.domain.geojson;

public class GeoJsonCustomer {

    public GeoJsonGeometry getGeometry() {
        return geometry;
    }
    public void setGeometry(GeoJsonGeometry geometry) {
        this.geometry = geometry;
    }
    public GeoJsonCustomerProperties getProperties() {
        return properties;
    }
    public void setProperties(GeoJsonCustomerProperties properties) {
        this.properties = properties;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    private GeoJsonGeometry geometry;
    private GeoJsonCustomerProperties properties;
    private String type;

    public GeoJsonCustomer(GeoJsonGeometry geometry, GeoJsonCustomerProperties properties, String type) {
        super();
        this.geometry = geometry;
        this.properties = properties;
        this.type = type;
    }

}
