package io.ordiy.github.data.ip_geoinfo.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import com.google.common.cache.LoadingCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Location;
import io.ordiy.github.data.ip_geoinfo.pojo.CollectGeoInfoV1Pojo;
import org.apache.commons.lang3.StringUtils ;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class Geo2IPInfoService {

    @Value("${my-config.geolite2-file.path:./GeoLite2-City.mmdb}")
    private String geoLite2File;

    private static final Logger logger = LoggerFactory.getLogger(Geo2IPInfoService.class);

    private static final String IPV4_PATTERN =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){2}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String IPV6_PATTERN =
            "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}(([0-9]{1,3}\\.){3}[0-9]{1,3})|([0-9a-fA-F]{1,4}:){1,4}:(([0-9]{1,3}\\.){3}[0-9]{1,3}))$";

    private LoadingCache<String, CollectGeoInfoV1Pojo> ipGeoInfoLoadingCache ;

    // 使用 AtomicReference 确保线程安全的 DatabaseReader 更新
    private final AtomicReference<DatabaseReader> databaseReaderRef = new AtomicReference<>();

    @PostConstruct
    public void  initCache() throws IOException {
        initGeoLiteDatabase();
        //init cache
        // 5000 IP cache , lastAccess  30 minutes 过期
        ipGeoInfoLoadingCache = CacheBuilder.newBuilder()
                .maximumSize(5000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build(new CacheLoader<String, CollectGeoInfoV1Pojo>() {
                    @Override
                    public CollectGeoInfoV1Pojo load(final String s) throws Exception {
                        return queryIpGeoInfo(s);
                    }
                });
    }

    // 每1天（1 day）重新加载一次
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 1 天，单位毫秒
    public void reloadDatabaseReader()  {
        try {
            logger.info(" scheduler will start reloadDatabaseReader...");
            File geoDatabaseFile = getGeoDatabaseFile();
            //更新 GetLite2 file 使用mv 操作，保证原子性
            DatabaseReader newReader = new DatabaseReader.Builder(geoDatabaseFile).build();
            databaseReaderRef.set(newReader); // 原子更新
            logger.info("GeoLite2-City.mmdb reloaded from: {},file version:{}" ,
                    geoDatabaseFile.getName(),
                    databaseReaderRef.get().getMetadata().getBuildDate());
        } catch (Exception e) {
            logger.error("reloadDatabaseReader error: ", e);
        }
    }


    public CollectGeoInfoV1Pojo queryIpGeoInfoFormCache(String ipAddress)  {
        CollectGeoInfoV1Pojo resultPojo = null;
        if (StringUtils.isNotBlank(ipAddress) &&
                (Pattern.matches(IPV4_PATTERN, ipAddress) || Pattern.matches(IPV6_PATTERN, ipAddress))) {
            try {
                resultPojo = ipGeoInfoLoadingCache.get(ipAddress);
            } catch (ExecutionException e) {
               logger.info("query ip geo info form cache failed.cause:", e);
            }
        }
        return resultPojo;
    }

    /**
     * 查询IP geo 信息 ， 直接从geoLite2 file中查询
     * @param ipAddress ipv4 ipv6 address
     * @return CollectGeoInfoV1Pojo
     */
    public CollectGeoInfoV1Pojo queryIpGeoInfo(String ipAddress) {
        CollectGeoInfoV1Pojo result = null;
        //查询数据
        if (StringUtils.isNotBlank(ipAddress) &&
                ( Pattern.matches(IPV4_PATTERN, ipAddress) || Pattern.matches(IPV6_PATTERN, ipAddress))){
            try {
                java.net.InetAddress inetAddress = InetAddress.getByName(ipAddress);
                CityResponse response = databaseReaderRef.get().city(inetAddress);
                //city
                City city = response.getCity();
                String cityName = city.getName();

                //country
                String countryCode = response.getCountry().getIsoCode();
                //location
                Location location = response.getLocation();
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                Long geoNameId = response.getCity().getGeoNameId();
                String geoId = Objects.isNull(geoNameId) ? StringUtils.EMPTY :  Long.toString(geoNameId);
                result = new CollectGeoInfoV1Pojo(countryCode, cityName, latitude, longitude, geoId);
            } catch (IOException | GeoIp2Exception e) {
                logger.error("dbReader failed.cause:" , e );
            }
        }
        return result;
    }


    /**
     * 从人间中加载geoLite2
     */
    private void initGeoLiteDatabase() throws IOException {
        // 使用 ClassLoader 加载数据库文件
        // 如果路径以 "classpath:" 开头，从资源目录加载
        File geoDatabaseFile = getGeoDatabaseFile();
        databaseReaderRef.set(new DatabaseReader.Builder(geoDatabaseFile).build());
        logger.info("geoLite2 file:{} info , build_datetime:{}",
                geoLite2File, databaseReaderRef.get().getMetadata().getBuildDate());
    }

    private File getGeoDatabaseFile (){
        File databaseFile ;
        if (geoLite2File.startsWith("classpath:")) {
            String resourcePath = geoLite2File.replace("classpath:", "");
            databaseFile = new File(Objects.requireNonNull(this.getClass()
                    .getClassLoader()
                    .getResource(resourcePath)).getFile());
        } else {
            // 否则按文件系统路径加载
            databaseFile = Paths.get(geoLite2File).toFile();
        }
       return databaseFile;
    }

}
