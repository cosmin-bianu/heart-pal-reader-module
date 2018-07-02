# Heart Pal - Data Reading

This is the module for reading the data from the prototype.

## Getting Started

Our prototype is comprised of three modules:
```
-> Raspberry Pi Zero
-> ADS1110 Analogue to Digital Converter
-> Sparkfun Heart Monitoring Module
```

You will need to wire up the device which is done as following:

- Between the Raspberry Pi Zero and the ADS1110:
```
5V - VCC
0V - GND
SCL_PIN - SCL
SDA_PIN - SDA
```
- Between the ADS1110 and the Sparkfun Heart Monitoring Module:
```
VIN+ - OUTPUT
```
- Between the Sparkfun Heart Monitoring Module and the Raspberry Pi Zero:
```
3.3V - 3V
GND - 0V
```

### Prerequisites

You will need to have Java 1.8 installed on the Raspberry Pi Zero.
If you use Raspbian, it should come with Java preinstalled.

## Deployment

You will want to run the JAR file on startup with the other software because the project is made in such manner that it displays everything on a smartphonethat has the app installed. We suggest registering a service and enabling the service to run on startup using systemctl. If you don't know how to do any of that read [this great tutorial](https://medium.com/@benmorel/creating-a-linux-service-with-systemd-611b5c8b91d6).

## Contributing

This section will be updated soon.

## Versioning

This section will be updated soon.

## Authors

This project is made possible by the 'I.L. Caragiale' National College, Bucharest.

Project Leader: Florea Andrei

Lead Programmer: Bianu Cosmin

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details
