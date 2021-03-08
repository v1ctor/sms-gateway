# sms-gateway

The aim of this project is to receive SMS messages and forward them to Telegram accounts which subscribed to the forwarding.

This is project based on https://github.com/v1ctor/huawei-api-client which is the client to the web UI of [HUAWEI E8372h-320 LTE/4G 150 Mbps USB Mobile Wi-Fi Modem](https://www.amazon.co.uk/gp/product/B08CY7VF3S)

# Setup

## On Raspberry Pi (Version ARMv7)

```
docker build -f Dockerfile.arm .
docker run --restart=always --name=sms-gateway <IMAGE_ID> --token=<TELEGRAM TOKEN> --allowed_users=<COMMA SEPARATED LIST OF TELEGRAM USERS> --password=<MODEM PASSWORD> --username=<MODEM USERNAME>
```

TODO: raspbian network interfaces configuration
