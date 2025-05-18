package com.example.csaladireceptknyv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.csaladireceptknyv.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import activities.Recept;
import activities.cookBookActivity;
import csirkes.ujRecept;

public class ReceptReading extends AppCompatActivity {
    private TextView recName, hozzavalokText, leirasText;
    private ImageView editButton;
    private ImageView receptImage;
    private Recept recept;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recept_reading_2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recName = findViewById(R.id.Név);
        hozzavalokText = findViewById(R.id.hozzavalok);
        leirasText = findViewById(R.id.leiras);
        receptImage = findViewById(R.id.kepPreview);

        recept = (Recept) getIntent().getSerializableExtra("Recept");


        if (recept != null) {
            recName.setText(recept.getNév());

            StringBuilder hozzavalokStr = new StringBuilder();
            List<String> lista = recept.getHozzavalok();
            if (lista != null) {
                for (String hozzavalokText : recept.getHozzavalok()) {
                    hozzavalokStr.append("• ").append(hozzavalokText).append("\n");
                }

                hozzavalokText.setText("Hozzávalók:\n" + hozzavalokStr);
                if (recept.getLeiras() != null) {
                    leirasText.setText("Leírás:\n" + recept.getLeiras());
                } else {
                    leirasText.setText("Nincs leírás rögzítve!");
                }

                String base64 = recept.getImageBase64();
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        receptImage.setImageBitmap(bitmap);
                    } catch (IllegalArgumentException e) {
                        receptImage.setImageBitmap(null);
                    }
                }
            }
        }

        editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReceptReading.this, ujRecept.class);
            intent.putExtra("Recept", recept); // Fontos: a Recept osztálynak implementálnia kell a Serializable interfészt
            startActivity(intent);
        });
        ImageView deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            if (recept != null && recept.getNév() != null) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Recept törlése")
                        .setMessage("Biztosan törölni szeretnéd ezt a receptet?")
                        .setPositiveButton("Igen", (dialog, which) -> {
                            deleteRecipe(recept.getNév());
                        })
                        .setNegativeButton("Mégse", null)
                        .show();
            } else {
                Toast.makeText(this, "Nem található recept azonosító!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void deleteRecipe(String recName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("receptek").document(recName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Recept törölve", Toast.LENGTH_SHORT).show();
                    finish(); // vissza az előző képernyőre
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a törlés során: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        String origin = getIntent().getStringExtra("origin");

        if ("notification".equals(origin)) {
            Intent intent = new Intent(this, cookBookActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
