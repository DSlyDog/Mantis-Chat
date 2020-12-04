package net.whispwriting.mantischat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> messageList;

    public MessageAdapter(List<Messages> messageList){
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int i){
        Messages messages = messageList.get(i);
        viewHolder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount(){
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView messageText;
        private CircleImageView image;

        public MessageViewHolder(View view){
            super(view);

            messageText = (TextView) view.findViewById(R.id.messageText);
            image = (CircleImageView) view.findViewById(R.id.profile_image_conv);
        }
    }
}
