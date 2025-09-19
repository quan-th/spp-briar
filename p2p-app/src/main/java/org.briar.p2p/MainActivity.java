package org.briar.p2p;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.briarproject.bramble.core.Bramble;
import org.briarproject.bramble.core.BrambleConfig;
import org.briarproject.bramble.core.Peer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private Bramble bramble;
	private PeerAdapter peerAdapter;
	private List<Peer> peerList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RecyclerView recyclerView = findViewById(R.id.peerRecyclerView);
		peerAdapter = new PeerAdapter();
		recyclerView.setAdapter(peerAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		EditText messageEditText = findViewById(R.id.messageEditText);
		Button sendButton = findViewById(R.id.sendButton);

		// Initialize Bramble
		BrambleConfig config = new BrambleConfig();
		bramble = Bramble.getInstance(config);
		bramble.start();

		// Listen for peer changes
		bramble.getPeerManager().addPeerListener(new Peer.PeerListener() {
			@Override
			public void onPeerAdded(Peer peer) {
				updatePeerList();
			}

			@Override
			public void onPeerRemoved(Peer peer) {
				updatePeerList();
			}
		});

		// Send message to all peers
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = messageEditText.getText().toString().trim();
				if (!msg.isEmpty()) {
					bramble.getMessageManager().sendMessageToAll(msg);
					messageEditText.setText("");
				}
			}
		});
	}

	private void updatePeerList() {
		runOnUiThread(() -> {
			peerList.clear();
			peerList.addAll(bramble.getPeerManager().getPeers());
			peerAdapter.setPeers(peerList);
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bramble != null) bramble.stop();
	}
}
