package gr.hua.dit.smartt;

/**
 * Created by nsouliotis on 12/10/2015.
 */
public class GetLinesNearStop implements java.io.Serializable{
    String stopName;
    String directionid;

    public GetLinesNearStop(String stopName, String directionid) {
        this.stopName = stopName;
        this.directionid = directionid;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getdirectionid() {
        return directionid;
    }

    public void setdirectionid(String directionid) {
        this.directionid = directionid;
    }
}
