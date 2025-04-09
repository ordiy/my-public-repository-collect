
```shell
- 配置为系统服务
# JDK 17 


sudo bash -c 'cat > /usr/lib/systemd/system/app-my-ip-geo-info-service.service << EOF

[Unit]
Description=java-application-aws-cdn-log-to-kafka-mq
After=network.target
After=time-sync.target network-online.target
Wants=time-sync.target

[Service]
Type=simple
User=ubuntu
Group=ubuntu
Environment="PATH=/usr/local/bin:/usr/bin:/bin"
WorkingDirectory=/data/app-web-ip-geo-info-service
ExecStart=/data/app-web-ip-geo-info-service/start.sh
Restart=always
RestartSec=30
TimeoutStopSec=300
LimitNOFILE=65536
StandardOutput=null
StandardError=null

[Install]
WantedBy=multi-user.target

EOF'

sudo systemctl daemon-reload 

sudo systemctl  enable app-my-ip-geo-info-service.service
sudo systemctl status app-my-ip-geo-info-service.service
sudo systemctl restart app-my-ip-geo-info-service.service
sudo systemctl status app-my-ip-geo-info-service.service

```