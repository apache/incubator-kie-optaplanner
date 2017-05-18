package ws.arete.arms.engine.domain.geojson;

import java.util.Random;

public class GeoJsonRouteProperties {
    public Long getTrip() {
        return trip;
    }

    public void setTrip(Long trip) {
        this.trip = trip;
    }

    public String getStroke() {
        return stroke;
    }

    public void setStroke() {
        this.stroke = String.format("#%02x%02x%02x", randHue(), randHue(), randHue());
    }
    
    private Long trip;
    private String stroke;
    
    public static int randHue() {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((255) + 1);

        return randomNum;
    }
}
