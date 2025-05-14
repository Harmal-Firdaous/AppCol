package com.example.appco;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String chatId, senderId, receiverId;
    private FirebaseFirestore db;
    private EditText messageInput;
    private Button sendButton;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Erreur : ID du destinataire manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Créer un ID de conversation unique (ordre alphabétique pour éviter doublons)
        if (senderId.compareTo(receiverId) < 0) {
            chatId = senderId + "_" + receiverId;
        } else {
            chatId = receiverId + "_" + senderId;
        }

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        adapter = new MessageAdapter(messageList, senderId);

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        Message message = new Message(senderId, text, new Date().getTime());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(docRef -> messageInput.setText(""));
    }

    private void listenForMessages() {
        if (chatId == null || chatId.isEmpty()) return;

        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        messageList.add(doc.toObject(Message.class));
                    }
                    adapter.notifyDataSetChanged();
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                });
    }
}
