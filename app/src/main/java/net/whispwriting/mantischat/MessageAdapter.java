package net.whispwriting.mantischat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message> messageList;
    private FirebaseUser currentUser;
    private Context context;
    private RecyclerView messageView;

    public MessageAdapter(List<Message> messageList, Context context, RecyclerView messageView){
        this.messageList = messageList;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.context = context;
        this.messageView = messageView;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i){
        final Message message = messageList.get(i);

        if (message.getFrom().equals(currentUser.getUid())){
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.image.setVisibility(View.INVISIBLE);
            viewHolder.receivedImage.setVisibility(View.INVISIBLE);
            if (message.getType().equals("image")){
                viewHolder.sentMessageText.setVisibility(View.INVISIBLE);
                Picasso.get().load(message.getMessage()).into(viewHolder.sentImage);
                viewHolder.sentImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri =  Uri.parse(message.getMessage());
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        String mime = "*/*";
                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        if (mimeTypeMap.hasExtension(
                                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
                            mime = mimeTypeMap.getMimeTypeFromExtension(
                                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                        intent.setDataAndType(uri,mime);
                        context.startActivity(intent);
                    }
                });
            }else {
                viewHolder.sentImage.setVisibility(View.INVISIBLE);
                viewHolder.sentMessageText.setText(message.getMessage());
            }
        }else{
            viewHolder.sentMessageText.setVisibility(View.INVISIBLE);
            viewHolder.sentImage.setVisibility(View.INVISIBLE);
            FirebaseFirestore.getInstance().collection("Users").document(message.getFrom())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            Picasso.get().load(document.getString("image"))
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.avatar)
                                    .into(viewHolder.image);
                        }
                    }
                }
            });
            if (message.getType().equals("image")){
                viewHolder.messageText.setVisibility(View.INVISIBLE);
                Picasso.get().load(message.getMessage()).into(viewHolder.receivedImage);
                viewHolder.receivedImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri =  Uri.parse(message.getMessage());
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        String mime = "*/*";
                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        if (mimeTypeMap.hasExtension(
                                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
                            mime = mimeTypeMap.getMimeTypeFromExtension(
                                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                        intent.setDataAndType(uri,mime);
                        context.startActivity(intent);
                    }
                });
            }else {
                viewHolder.receivedImage.setVisibility(View.INVISIBLE);
                viewHolder.messageText.setText(message.getMessage());
            }
        }
    }

    @Override
    public int getItemCount(){
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView messageText, sentMessageText;
        private ImageView receivedImage, sentImage;
        private CircleImageView image;

        public MessageViewHolder(View view){
            super(view);

            messageText = (TextView) view.findViewById(R.id.messageText);
            sentMessageText = (TextView) view.findViewById(R.id.sentMessageText);
            receivedImage = (ImageView) view.findViewById(R.id.receivedImage);
            sentImage = (ImageView) view.findViewById(R.id.sentImage);
            image = (CircleImageView) view.findViewById(R.id.profile_image_message);
        }
    }
}
