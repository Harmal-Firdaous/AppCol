package com.example.appco;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class LocataireDashboard extends AppCompatActivity {

    private EditText editTextCountry, editTextCity;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private AnnonceAdapter adapter;
    private List<Annonce> annonceList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc);

        editTextCountry = findViewById(R.id.editTextCountry);
        editTextCity = findViewById(R.id.editTextCity);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerView = findViewById(R.id.recyclerViewAnnonces);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnnonceAdapter(annonceList,this );
        recyclerView.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> searchAnnonces());
    }

    private void searchAnnonces() {
        String country = editTextCountry.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();

        db.collection("annonces")
                .whereEqualTo("country", country)
                .whereEqualTo("city", city)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    annonceList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Annonce annonce = doc.toObject(Annonce.class);
                        annonce.setId(doc.getId());
                        annonceList.add(annonce);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur de recherche", Toast.LENGTH_SHORT).show());
    }
}

