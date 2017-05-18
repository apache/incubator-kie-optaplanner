package ws.arete.arms.engine.domain.geojson;

public class GeoJsonRoute {

    public GeoJsonGeometry getGeometry() {
        return geometry;
    }
    public void setGeometry(GeoJsonGeometry geometry) {
        this.geometry = geometry;
    }
    public GeoJsonRouteProperties getProperties() {
        return properties;
    }
    public void setProperties(GeoJsonRouteProperties properties) {
        this.properties = properties;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    private GeoJsonGeometry geometry;
    private GeoJsonRouteProperties properties;
    private String type;
    
    public GeoJsonRoute(GeoJsonGeometry geometry, GeoJsonRouteProperties properties, String type) {
        super();
        this.geometry = geometry;
        this.properties = properties;
        this.type = type;
    }
}
