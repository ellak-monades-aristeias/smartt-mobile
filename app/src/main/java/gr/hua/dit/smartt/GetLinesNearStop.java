package gr.hua.dit.smartt;

/**
 * Created by nsouliotis on 12/10/2015.
 */
public class GetLinesNearStop implements java.io.Serializable{
    String stopName;
    String directionid;
    int tracked;

    public GetLinesNearStop(String stopName, String directionid, int tracked) {
        this.stopName = stopName;
        this.directionid = directionid;
        this.tracked = tracked;
    }

    public int getTracked() {
        return tracked;
    }

    public void setTracked(int tracked) {
        this.tracked = tracked;
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
