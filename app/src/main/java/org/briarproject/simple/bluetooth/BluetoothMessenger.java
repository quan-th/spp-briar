package org.briarproject.simple.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("MissingPermission")
public class BluetoothMessenger {
    
    private static final String TAG = "BluetoothMessenger";
    private static final String SERVICE_NAME = "BriarBluetoothMessenger";
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private final Context context;
    private final MessageListener listener;
    private final BluetoothAdapter bluetoothAdapter;
    private final ExecutorService executor;
    
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket clientSocket;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    
    private boolean isRunning = false;
    
    public BluetoothMessenger(Context context, MessageListener listener) {
        this.context = context;
        this.listener = listener;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.executor = Executors.newCachedThreadPool();
    }
    
    public void start() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available or not enabled");
            return;
        }
        
        isRunning = true;
        startAcceptThread();
        
        // Register for device discovery
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(discoveryReceiver, filter);
    }
    
    public void stop() {
        isRunning = false;
        
        try {
            context.unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
        
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        
        executor.shutdown();
    }
    
    public void sendMessage(String message) {
        if (connectedThread != null) {
            connectedThread.write(message.getBytes());
            listener.onMessageSent(message);
        }
    }
    
    public void startDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        
        // Try to connect to paired devices first
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            connectToDevice(device);
            break; // Connect to first paired device for simplicity
        }
        
        // Start discovery for new devices
        bluetoothAdapter.startDiscovery();
    }
    
    private void startAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }
    
    private void connectToDevice(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
        }
        
        connectThread = new ConnectThread(device);
        connectThread.start();
    }
    
    private void manageConnection(BluetoothSocket socket) {
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        
        String deviceAddress = socket.getRemoteDevice().getAddress();
        listener.onConnectionEstablished(deviceAddress);
    }
    
    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // Try to connect to discovered device
                    connectToDevice(device);
                }
            }
        }
    };
    
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }
        
        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (isRunning) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }
                
                if (socket != null) {
                    manageConnection(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    break;
                }
            }
        }
        
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
    
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            
            try {
                tmp = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }
        
        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            
            manageConnection(mmSocket);
        }
        
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input/output streams", e);
            }
            
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            
            while (isRunning) {
                try {
                    bytes = mmInStream.read(buffer);
                    String receivedMessage = new String(buffer, 0, bytes);
                    String senderAddress = mmSocket.getRemoteDevice().getAddress();
                    listener.onMessageReceived(receivedMessage, senderAddress);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    listener.onConnectionLost(mmSocket.getRemoteDevice().getAddress());
                    break;
                }
            }
        }
        
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }
        
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}