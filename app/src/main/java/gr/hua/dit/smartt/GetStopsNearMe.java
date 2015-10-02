package gr.hua.dit.smartt;

/**
 * Created by nsouliotis on 1/10/2015.
 */
public class GetStopsNearMe implements java.io.Serializable{
    String stopName;
    String id;
    double stopLat;
    double stopLng;

    public GetStopsNearMe(String stopName, String id, double stopLat, double stopLng) {
        this.stopName = stopName;
        this.id = id;
        this.stopLat = stopLat;
        this.stopLng = stopLng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStopName() {
        return stopName;
    }

    public double getStopLat() {
        return stopLat;
    }

    public double getStopLng() {
        return stopLng;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public void setStopLat(double stopLat) {
        this.stopLat = stopLat;
    }

    public void setStopLng(double stopLng) {
        this.stopLng = stopLng;
    }
}
