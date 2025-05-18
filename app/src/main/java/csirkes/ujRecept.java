package csirkes;

import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.csaladireceptknyv.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import activities.Recept;


public class ujRecept extends AppCompatActivity {

    private EditText recName, Hozzavalok, Leiras;
    private Button Mentés;

    private FirebaseFirestore db;
    private String kategoria;
    private String base64Kep = null;
    private Button fotoGomb;
    private String eredetiNev;
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_uj_recept);

        db = FirebaseFirestore.getInstance();

        recName = findViewById(R.id.recName);
        Hozzavalok = findViewById(R.id.Hozzavalok);
        Leiras = findViewById(R.id.Leiras);
        Mentés = findViewById(R.id.save);


        ImageButton fotoGomb = findViewById(R.id.photo);

        Mentés.setOnClickListener(v -> mentsReceptet());

        fotoGomb.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Recept recept = (Recept) getIntent().getSerializableExtra("Recept");

        if (recept != null) {
            eredetiNev = recept.getNév();
            recName.setText(recept.getNév());
            Leiras.setText(recept.getLeiras());
            kategoria = recept.getKategoria();

            if (kategoria == null || kategoria.isEmpty()) {
                Toast.makeText(this, "Hiba: a receptnek nincs kategóriája!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            List<String> hozzavalokLista = recept.getHozzavalok();
            if (hozzavalokLista != null) {
                StringBuilder sb = new StringBuilder();
                for (String hozzaval : hozzavalokLista) {
                    sb.append(hozzaval).append("\n");
                }
                Hozzavalok.setText(sb.toString());
            } else {
                Hozzavalok.setText("");
            }
        }else {
            eredetiNev = null; // új receptnél nincs eredeti név
            kategoria = getIntent().getStringExtra("kategoria");

            if (kategoria == null || kategoria.isEmpty()) {
                Toast.makeText(this, "Hiba: nincs kategória megadva!", Toast.LENGTH_SHORT).show();
                finish(); // kilép, ha nem kapott kategóriát
            }
        }

    }

    private void mentsReceptet() {
        String nev = recName.getText().toString().trim();
        String hozzavalokstr = Hozzavalok.getText().toString().trim();
        String leirasStr = Leiras.getText().toString().trim();

        if (nev.isEmpty() || hozzavalokstr.isEmpty() || leirasStr.isEmpty()) {
            Toast.makeText(this, "Kérlek tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> hozzavalok = Arrays.asList(hozzavalokstr.split("\\s*,\\s*"));

        Map<String, Object> recept = new HashMap<>();
        recept.put("Név", nev);

        recept.put("kategoria", kategoria);
        recept.put("hozzavalok", hozzavalok);
        recept.put("leiras", leirasStr);

        if (base64Kep != null) {
            recept.put("imageBase64", base64Kep);
        }

        if (eredetiNev != null && !eredetiNev.equals(nev))  {
            // Ha a felhasználó átírta a nevet, akkor töröld a régit, és mentsd az újat
            db.collection("receptek").document(eredetiNev).delete()
                    .addOnSuccessListener(aVoid -> {
                        db.collection("receptek").document(nev)
                                .set(recept)
                                .addOnSuccessListener(doc -> {
                                    Toast.makeText(this,"Sikeres mentés!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,"Hiba történt!", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,"Hiba történt a régi törlésekor!", Toast.LENGTH_SHORT).show());
        } else {
            // ha új vagy nem változott a név, sima mentés
            db.collection("receptek").document(nev)
                    .set(recept)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this,"Sikeres mentés!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,"Hiba történt!", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap imageBitmap = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }
        // Base64 konvertálás
        if (imageBitmap != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            base64Kep = Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    }
}