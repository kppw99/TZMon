# tzMon
The TZMon is the security framework for a mobile game application.
This framework is specially designed for an integrity check, secure update, abusing detection, data hiding, and timer synchronization using **ARM TrustZone**.

TZMon uses AOSP(Android Open Source Project) as a Normal OS and OPTEE as a Secure OS.
Target Board is **hikey960**. Refer to the link for more information. [[here](https://www.96boards.org/product/hikey960/)]

There are two modes for TZMon; SIM_MODE, and TARGET_MODE.
A TARGET_MODE use hikey960 board as a mobile environment.
SIM_MODE can make applying and verifying TZMon easy.

The simulation environments are as like below:
- Mobile Game Apps (including TZMon library): Android Studio Simulator
- TZMon Trusted Application: hikey960 board
- S2B (for connection with the simulator and the target board): Linux machine (independent of kernel version and linux distro)
- Update Server (for secure update protocol): Linux machine (independent of kernel version and linux distro)

## Getting Started with tzMon

### Prerequisites
- Preparing for the mobile game source code.
- Applying the JNI environment to the game. [[Android Developers](https://developer.android.com/ndk/samples/sample_hellojni.html)]
- Adding permission option to the AndroidManifest.xml file.
```
  <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions" />
  <uses-permission android:name="android.permission.INTERNET" />
```
- Allowing usage access in the smart-phone or simulator.

<img src="./img/usage_access.png" width="250"></img>
- Execution update_server and S2B. (for SIM_MODE): update_server and s2b are running on linux machine. Two application must use same port number
```
  ./update_server [port_number]
  ./s2b_server [port_number]
```

### Usage
1. Loading the TZMon library formed as JNI
```
  static {
    System.loadLibrary("tzMonJNI");
  }
```

You can see the help message below by passing an '-h' (or '--help') argument.
```
TBD
```

## About
This program is authored and maintained by **Sanghoon Jeon**
> GitHub [@kppw99](https://github.com/kppw99/tzMon)

