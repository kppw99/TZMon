# tzMon
The TZMon is the security framework for a mobile game application.
This framework is specially designed for an integrity check, secure update,
abusing detection, data hiding, and timer synchronization using ARM TrustZone.

TZMon uses AOSP(Android Open Source Project) as a Normal OS and OPTEE as a Secure OS.
Target Board is hikey960. Refer to the link for more information. [here](https://www.96boards.org/product/hikey960/)

There are two modes for TZMon; SIM_MODE, and TARGET_MODE.
A TARGET_MODE use hikey960 board as a mobile environment.
SIM_MODE can make applying and verifying TZMon easy.

The simulation environment is as like below:
- Mobile Game Apps (including TZMon library): Android Studio Simulator
- TZMon Trusted Application: hikey960 board
- S2B (for connection with the simulator and the target board): Linux machine (independent of kernel version and linux distro)
- Update Server (for secure update protocol): Linux machine (independent of kernel version and linux distro)

## Getting Started with tzMon

### Prerequisites
- **TBD**

### Usage
1. TBD

You can see the help message below by passing an '-h' (or '--help') argument.
```
TBD
```

## About
This program is authored and maintained by **Sanghoon Jeon**
> GitHub [@kppw99](https://github.com/kppw99/tzMon)

