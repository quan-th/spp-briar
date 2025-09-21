package org.briarproject.simple.bluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    
    private final List<Message> messages;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView senderText;
        private final TextView timeText;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            senderText = itemView.findViewById(R.id.senderText);
            timeText = itemView.findViewById(R.id.timeText);
        }
        
        public void bind(Message message) {
            messageText.setText(message.getContent());
            senderText.setText(message.getSender());
            timeText.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            // Style differently for sent vs received messages
            if (message.isSentByMe()) {
                itemView.setBackgroundResource(R.drawable.message_sent_background);
                senderText.setVisibility(View.GONE);
            } else {
                itemView.setBackgroundResource(R.drawable.message_received_background);
                senderText.setVisibility(View.VISIBLE);
            }
        }
    }
}