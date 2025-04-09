package io.ordiy.github.data.ip_geoinfo.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
public class CollectGeoInfoV1Pojo {

    public CollectGeoInfoV1Pojo(String countryCode, String city, Double lat, Double lon, String geoId) {
        this.countryCode = countryCode;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
        this.geoId = geoId;
    }

    @JsonProperty("country_code")
    public String countryCode;

    @JsonProperty("city")
    public String city ;

    public Double lat;

    public Double lon ;

    @JsonProperty("geo_id")
    public String geoId;

}
