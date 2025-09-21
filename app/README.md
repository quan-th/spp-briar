# Simple Bluetooth Messenger using Bramble Protocol

This is a simple Android application that demonstrates Bluetooth messaging using concepts from the Bramble protocol used in Briar.

## Features

- **Bluetooth Discovery**: Automatically discovers nearby Bluetooth devices
- **Peer-to-Peer Messaging**: Send and receive messages directly between devices
- **Simple UI**: Clean interface showing message history
- **Connection Management**: Handles Bluetooth connections and reconnections

## Architecture

The application consists of several key components:

### Core Classes

1. **MainActivity**: Main UI controller that handles user interactions
2. **BluetoothMessenger**: Core Bluetooth communication handler
3. **MessageListener**: Interface for handling Bluetooth events
4. **Message**: Data class representing chat messages
5. **MessageAdapter**: RecyclerView adapter for displaying messages

### Bluetooth Communication

The app uses Android's Bluetooth API with RFCOMM sockets for communication:

- **Server Socket**: Listens for incoming connections
- **Client Socket**: Connects to discovered devices
- **Message Protocol**: Simple text-based messaging

## Usage

1. **Enable Bluetooth**: The app will prompt to enable Bluetooth if not already enabled
2. **Grant Permissions**: Allow Bluetooth and location permissions when prompted
3. **Discover Devices**: Tap "Discover Devices" to find nearby devices running the same app
4. **Send Messages**: Type messages and tap "Send" to communicate with connected devices

## Permissions Required

- `BLUETOOTH`: Basic Bluetooth functionality
- `BLUETOOTH_ADMIN`: Bluetooth device discovery
- `ACCESS_FINE_LOCATION`: Required for Bluetooth discovery on Android 6+
- `BLUETOOTH_CONNECT`: Connect to Bluetooth devices (Android 12+)
- `BLUETOOTH_SCAN`: Scan for Bluetooth devices (Android 12+)
- `BLUETOOTH_ADVERTISE`: Make device discoverable (Android 12+)

## Technical Details

### Bramble Protocol Inspiration

While this is a simplified implementation, it draws inspiration from Briar's Bramble protocol:

- **Direct Device Communication**: No central server required
- **Opportunistic Networking**: Connects when devices are in range
- **Privacy-Focused**: Local communication only

### Connection Flow

1. App starts a Bluetooth server socket listening for connections
2. Discovers nearby devices and attempts connections
3. Establishes RFCOMM connection using standard UUID
4. Exchanges messages as byte arrays over the socket
5. Handles connection loss and reconnection

## Building and Running

1. Open the project in Android Studio
2. Ensure you have Android SDK 21+ installed
3. Build and run on two or more Android devices with Bluetooth
4. Grant permissions and enable Bluetooth on both devices
5. Use "Discover Devices" to establish connections

## Limitations

This is a simplified demonstration and has several limitations compared to the full Briar implementation:

- **No Encryption**: Messages are sent in plain text
- **No Authentication**: No verification of device identity
- **Single Connection**: Only handles one connection at a time
- **No Message Persistence**: Messages are not stored permanently
- **Basic Discovery**: Simple device discovery without advanced networking

## Future Enhancements

To make this more like the full Bramble protocol, consider adding:

- Message encryption and authentication
- Multiple simultaneous connections
- Message queuing and persistence
- Advanced peer discovery mechanisms
- Network resilience and reconnection logic