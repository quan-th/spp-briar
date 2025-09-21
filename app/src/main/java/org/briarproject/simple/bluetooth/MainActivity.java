package org.briarproject.simple.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MessageListener {
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    
    private BluetoothMessenger messenger;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    
    private EditText messageInput;
    private Button sendButton;
    private Button discoverButton;
    private TextView statusText;
    private RecyclerView messagesRecyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerView();
        checkBluetoothPermissions();
    }
    
    private void initViews() {
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        discoverButton = findViewById(R.id.discoverButton);
        statusText = findViewById(R.id.statusText);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
            }
        });
    }
    
    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);
    }
    
    private void checkBluetoothPermissions() {
        String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        };
        
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            initializeBluetooth();
        }
    }
    
    private void initializeBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            statusText.setText("Bluetooth not supported");
            return;
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startBluetoothMessenger();
        }
    }
    
    private void startBluetoothMessenger() {
        messenger = new BluetoothMessenger(this, this);
        messenger.start();
        statusText.setText("Bluetooth messenger started");
        sendButton.setEnabled(true);
        discoverButton.setEnabled(true);
    }
    
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty() && messenger != null) {
            messenger.sendMessage(text);
            messageInput.setText("");
        }
    }
    
    private void startDiscovery() {
        if (messenger != null) {
            messenger.startDiscovery();
            statusText.setText("Discovering devices...");
        }
    }
    
    @Override
    public void onMessageReceived(final String message, final String senderAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.add(new Message(message, senderAddress, false));
                messageAdapter.notifyItemInserted(messages.size() - 1);
                messagesRecyclerView.scrollToPosition(messages.size() - 1);
            }
        });
    }
    
    @Override
    public void onMessageSent(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.add(new Message(message, "You", true));
                messageAdapter.notifyItemInserted(messages.size() - 1);
                messagesRecyclerView.scrollToPosition(messages.size() - 1);
            }
        });
    }
    
    @Override
    public void onConnectionEstablished(final String deviceAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Connected to: " + deviceAddress);
                Toast.makeText(MainActivity.this, "Connected to device", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onConnectionLost(final String deviceAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Connection lost");
                Toast.makeText(MainActivity.this, "Connection lost", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startBluetoothMessenger();
            } else {
                statusText.setText("Bluetooth required for messaging");
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                initializeBluetooth();
            } else {
                statusText.setText("Bluetooth permissions required");
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messenger != null) {
            messenger.stop();
        }
    }
}