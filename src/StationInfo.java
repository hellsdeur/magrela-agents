public class StationInfo implements jade.util.leap.Serializable {
    float latitude;
    float longitude;
    int bikeCount;
    int dockcount;

    public StationInfo(float latitude, float longitude, int bikeCount, int dockcount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikeCount = bikeCount;
        this.dockcount = dockcount;
    }
}
