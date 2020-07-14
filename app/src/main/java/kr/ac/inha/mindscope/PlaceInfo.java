package kr.ac.inha.mindscope;

public class PlaceInfo {
    public String placeName;
    public String placeAddress;
    public String placeUserName;
    public Double placeLat;
    public Double placeLng;

    public PlaceInfo(String placeName, String placeAddress, String placeUserName, Double placeLat, Double placeLng){
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.placeUserName = placeUserName;
        this.placeLat = placeLat;
        this.placeLng = placeLng;
    }

}
