# Generating test credentials

```bash
openssl genrsa -out server.key.rsa 2048
openssl pkcs8 -topk8 -in server.key.rsa -out server.key -nocrypt
rm server.key.rsa
openssl req -new -x509 -sha256 -key server.key -out server.crt -days 3650
```

When prompted for certificate information, everything is default except the
common name which is set to `grpc-dropwizard.example.com`.
