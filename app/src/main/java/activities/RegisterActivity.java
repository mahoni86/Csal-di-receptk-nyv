package activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.csaladireceptknyv.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register); // Activity layout beállítása

        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth inicializálása

        // Rendszerbarok kezelése, hogy ne takarják el az UI-t
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Regisztrációs metódus
    public void register(View view) {
        // Az EditText-ek lekérése
        EditText emailEditText = findViewById(R.id.username); // Az email mező
        EditText passwordEditText = findViewById(R.id.password); // A jelszó mező

        // A felhasználói adatokat kinyerjük
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Ellenőrizzük, hogy a mezők üresek-e
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Kérlek töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Regisztráció a Firebase Authentication-nel
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("success_message", "Sikeres regisztráció" + (user != null ? user.getEmail() : ""));
                        startActivity(intent);
                        finish();
                    } else {
                        Exception exception = task.getException();
                        Toast.makeText(RegisterActivity.this, "Regisztrációs hiba: " + (exception != null ? exception.getMessage() : "Ismeretlen hiba"), Toast.LENGTH_LONG).show();
                    }
                    Log.e("FIREBASE_REG", "Regisztráció sikertelen", task.getException());
                });
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
}
