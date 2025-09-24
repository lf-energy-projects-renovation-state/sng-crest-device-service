<!--
SPDX-FileCopyrightText: Copyright Contributors to the GXF project

SPDX-License-Identifier: Apache-2.0
-->
# Crest Device Service

## PSK change flow

When a shipment file is imported, the key and secret in the shipment file will be set as the active
PSK.
Also, a new PSK will be generated and set to status `READY`.

To test this flow locally:

1. If you have previously run the crest device service with the `test-data` profile, and you wish to
   test this new flow, delete the database container to clear the existing data, then run the
   service without the `test-data` profile.
2. Set `crest-device-service.psk.change-initial-psk` to `true`.
3. Run the crest device service, the shipment file processor and maki connect light.
4. Upload the `test_shipment_file.json` file to the shipment file processor.
5. There should be an active and a ready psk in the database.
6. Run the coap-http proxy.
7. Run the simulator.
8. When the crest device service receives a message from the device (simulator), a psk set command will be sent in the downlink response to the device (simulator). The new key will be set to pending.
    1. When the crest device service receives a success result code in the subsequent message from the device (simulator), the pending key will be set to active and the old key will become inactive.
    2. When the crest device service receives a failure result code in the subsequent message from the device (simulator), the pending key will be set to invalid.


## Mutual TLS
Communication between the CoAP HTTP Proxy and the Crest device service should be encrypted using mutual TLS.

The repositories contain test certificates that can be used for local testing. (they are not included in the jar or docker image)
They can be also be (re)generated using the [generate_certificates.sh](scripts/generate_certificates.sh) script.

## Active Directory / LDAP for local development
The docker-compose file starts Samba in Domain Controller mode, which provides an Active Directory server.
It cannot be provisioned from build time, so the first time you run it, it will be empty.
To populate it:
1. Start the docker-compose services: `docker-compose up -d`
2. Open a shell in the `samba-ad` container: `docker exec -it samba-ad /bin/bash`
3. Run the following commands and remember the user passwords you supply:
```bash
echo "Creating user 'overdruk'"
samba-tool user create overdruk --given-name Over --surname Druk
echo "Creating user 'lampjeaan'"
samba-tool user create lampjeaan --given-name Lampje --surname Aan 
samba-tool group create flex 
samba-tool group create kod 
samba-tool group addmembers kod overdruk 
samba-tool group addmembers flex lampjeaan
```

If you want to start over with a fresh configuration, simply delete the Samba data folder as root (`sudo rm <source-root>/docker/samba-ad/samba-data`)

## Web apps
This service contains a web application that runs on the port specified by the property `config.web-server.port`.
For the `dev` profile this is port 9001. http://localhost:9001/web will get you there.

_'web' is required to distinguish the web app requests from CoAP(S) requests. They pass through the same filter chain._

You have to log in to access the app (using the passwords you supplied to Samba). 
There are two users in the LDAP server (provided by `docker-compose.yaml`):
- `overdruk`: part of the group 'kod', which has access
- `lampjeaan`: part of the group 'flexovl', which has _no_ access and can be used to test the access control

(use a browser that supports 'unsafe' (non-https) connections, e.g. Firefox, Brave, etc.)

