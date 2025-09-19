package org.briar.p2p;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.briarproject.bramble.core.Peer;

import java.util.ArrayList;
import java.util.List;

public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.PeerViewHolder> {

	private final List<Peer> peers = new ArrayList<>();

	public void setPeers(List<Peer> newPeers) {
		peers.clear();
		peers.addAll(newPeers);
		notifyDataSetChanged();
	}

	@Override
	public PeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(android.R.layout.simple_list_item_2, parent, false);
		return new PeerViewHolder(view);
	}

	@Override
	public void onBindViewHolder(PeerViewHolder holder, int position) {
		Peer peer = peers.get(position);
		holder.nameText.setText(peer.getName());
		holder.statusText.setText(peer.isOnline() ? "Online" : "Offline");
	}

	@Override
	public int getItemCount() {
		return peers.size();
	}

	static class PeerViewHolder extends RecyclerView.ViewHolder {
		TextView nameText;
		TextView statusText;

		PeerViewHolder(View itemView) {
			super(itemView);
			nameText = itemView.findViewById(android.R.id.text1);
			statusText = itemView.findViewById(android.R.id.text2);
		}
	}
}
