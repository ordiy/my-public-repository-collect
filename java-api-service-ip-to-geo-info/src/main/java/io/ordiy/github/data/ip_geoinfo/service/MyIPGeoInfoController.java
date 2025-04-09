package io.ordiy.github.data.ip_geoinfo.service;

//import lombok.extern.log4j.Log4j2;

import io.ordiy.github.data.ip_geoinfo.pojo.CollectGeoInfoV1Pojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController()
public class MyIPGeoInfoController {
    private static final Logger logger = LoggerFactory.getLogger(MyIPGeoInfoController.class);

    @Autowired
    private Geo2IPInfoService geo2IPInfoService;
    /**
     * ip to geo_country
     * @param ip_addr
     * @return
     */
    @GetMapping("/data_v1/get_ip_geo_info")
    public ResponseEntity<CollectGeoInfoV1Pojo> getGeoCountryInfo(@RequestParam(value = "ip_addr",
            defaultValue = "") String ipAddr) {
        logger.debug("get---> ip_geo_info_v1 ,ip:{}", ipAddr);
        CollectGeoInfoV1Pojo pojo = geo2IPInfoService.queryIpGeoInfoFormCache(ipAddr);

        ResponseEntity<CollectGeoInfoV1Pojo> response = Optional.ofNullable(pojo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        return response;
    }

    /**
     * 路径获取
     *
     * @param ipAddress
     * @return
     */
    @GetMapping("/data_v1/ip_geo_info/{ip_address}")
    public ResponseEntity<CollectGeoInfoV1Pojo> getGeoCountryInfoByPath(@PathVariable("ip_address") String ipAddress) {
        logger.debug("get---> path ip_geo_info_v1 ,ip:{}", ipAddress);
        CollectGeoInfoV1Pojo pojo = geo2IPInfoService.queryIpGeoInfoFormCache(ipAddress);
        ResponseEntity<CollectGeoInfoV1Pojo> response = Optional.ofNullable(pojo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        return response;
    }

}
