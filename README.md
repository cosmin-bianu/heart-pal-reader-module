# Heart Pal - Data Reading

Java application used to read ECG data from the Sparkfun Heart Monitoring module on the Rapsberry Pi Zero W. This has been used to transmit ECG data to the Heart Pal Android application for processing.

## Getting Started

Our prototype has been built with these three modules:

* [Raspberry Pi Zero W](https://www.raspberrypi.org/products/raspberry-pi-zero-w/)
* [ADS1110 Analogue to Digital Converter](https://picclick.co.uk/1pcs-16-bits-Analog-to-Digital-Converter-Module-202034119772.html)
* [Sparkfun Heart Monitoring Module](https://www.sparkfun.com/products/12650)
* [Sparkfun Electrode Sensor Cables](https://www.sparkfun.com/products/12970)
* [Sparkfun Electrode Pads](https://www.sparkfun.com/products/12969)

Our wiring looked something like this

- Between the Raspberry Pi Zero and the ADS1110:
```
5V <-> VCC
0V <-> GND
SCL_PIN <-> SCL
SDA_PIN <-> SDA
```
- Between the ADS1110 and the Sparkfun Heart Monitoring Module:
```
VIN+ <-> OUTPUT
```
- Between the Sparkfun Heart Monitoring Module and the Raspberry Pi Zero:
```
3.3V <-> 3V
GND <-> 0V
```

### Prerequisites

You will need to have Java 1.8 installed on the Raspberry Pi Zero.
It should come with Java preinstalled on Raspbian.

## Deployment

You will want to run the JAR file on startup with the other software because the project is made in such manner that it displays everything on a smartphone that has the app installed. We suggest registering a service and enabling the service to run on startup using systemctl. [Here](https://medium.com/@benmorel/creating-a-linux-service-with-systemd-611b5c8b91d6) is a great tutorial on how do it.

## Authors

This project is made possible by the 'I.L. Caragiale' National College, Bucharest.

Project Leader: Florea Andrei

Lead Programmer: Bianu Cosmin

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details
