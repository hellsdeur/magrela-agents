public class InfoStation implements jade.util.leap.Serializable {
    String station;
    String address;
    float latitude;
    float longitude;
    int bikeCount;
    int dockcount;

    public InfoStation(String station, String address, float latitude, float longitude, int bikeCount, int dockcount) {
        this.station = station;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikeCount = bikeCount;
        this.dockcount = dockcount;
    }
}
