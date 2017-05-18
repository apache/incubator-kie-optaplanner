package ws.arete.arms.engine.domain.geojson;

public class GeoJsonCustomerProperties {
    public String getMarkersymbol() {
        return markersymbol;
    }

    public void setMarkersymbol(String markersymbol) {
        this.markersymbol = markersymbol;
    }

    public String getMarkersize() {
        return markersize;
    }

    public void setMarkersize(String markersize) {
        this.markersize = markersize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTwe() {
        return twe;
    }

    public void setTwe(Long twe) {
        this.twe = twe;
    }

    public Long getTws() {
        return tws;
    }

    public void setTws(Long tws) {
        this.tws = tws;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Double departureTime) {
        this.departureTime = departureTime;
    }


    private String markersymbol;
    private String markersize;
    private String name;
    private Long twe;
    private Long tws;
    private Long id;

    private Double deliveryRangeStart;
    private Double deliveryRangeEnd;
    private Double arrivalTime;
    private Double departureTime;

}
