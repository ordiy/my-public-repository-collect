#!/bin/bash
# 定期从github https://git.io/GeoLite2-City.mmdb
#备用下载地址 github https://github.com/P3TERX/GeoLite.mmdb/raw/download/GeoLite2-City.mmdb

#下载文件
pwd
echo " start download ip_geo  mmdb file "

mkdir -p ./tmp_dir
#先下载文件到临时目录，再使用mv移动文件（mv 是原子操作，避免在操作文件期间 程序reload 异常）

wget -O ./tmp_dir/GeoLite2-City.mmdb  https://git.io/GeoLite2-City.mmdb

mv ./tmp_dir/GeoLite2-City.mmdb ./