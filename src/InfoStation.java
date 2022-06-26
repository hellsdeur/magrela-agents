public class InfoStation implements jade.util.leap.Serializable {
    float latitude;
    float longitude;
    int bikeCount;
    int dockcount;

    public InfoStation(float latitude, float longitude, int bikeCount, int dockcount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikeCount = bikeCount;
        this.dockcount = dockcount;
    }
}
