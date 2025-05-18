package foMenu;


import android.content.Intent;
import android.os.Bundle;

import activities.Recept;
import activities.ReceptAdapter_2;
import csirkes.ujRecept;
import android.widget.ImageButton;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csaladireceptknyv.R;
import com.example.csaladireceptknyv.ReceptReading;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class sertes extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReceptAdapter_2 adapter;
    private List<Recept> receptLista = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sertes);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            TextView sertes = findViewById(R.id.sertes);
            ObjectAnimator animator = ObjectAnimator.ofFloat(sertes, "translationY", 600f, 10f);//animáció
            animator.setDuration(1000);
            animator.start();
            //végleges pozíció
            animator.addListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {

                    sertes.setTranslationY(10f);
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }
            });
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        db = FirebaseFirestore.getInstance();
        loadReceptek();

        ImageButton addButton = findViewById(R.id.addRecipeButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(sertes.this, ujRecept.class);
            intent.putExtra("kategoria", "sertes");
            startActivity(intent);
        });

        adapter = new ReceptAdapter_2(receptLista, recept -> {

            Intent intent = new Intent(sertes.this, ReceptReading.class);
            intent.putExtra("Recept", recept);
            startActivity(intent);




        });
        recyclerView.setAdapter(adapter);
    }
    private void loadReceptek() {
        db.collection("receptek")
                .whereEqualTo("kategoria", "sertes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    receptLista.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Recept recept = doc.toObject(Recept.class);
                        receptLista.add(recept);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt a receptek betöltése közben.", Toast.LENGTH_SHORT).show();
                });
               /* .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Hiba történt a receptek figyelése közben.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    receptLista.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Recept recept = doc.toObject(Recept.class);
                        receptLista.add(recept);
                    }
                    adapter.notifyDataSetChanged();
                });*/
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadReceptek();
    }
}