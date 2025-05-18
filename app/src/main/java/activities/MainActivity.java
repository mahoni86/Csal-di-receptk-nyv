package activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.csaladireceptknyv.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Sikeres regisztráció üzenetének ellenőrzése

        mAuth = FirebaseAuth.getInstance();

        String successMessage = getIntent().getStringExtra("success_message");
        if (successMessage != null) {
            Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });

        /*PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(WeeklyRecipeWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(workRequest);*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    public void login(View view) {

            EditText userName = findViewById(R.id.username);
            EditText password = findViewById(R.id.password);

            String userNamestr = userName.getText().toString().trim();
            String passwordstr = password.getText().toString().trim();

            if (userNamestr.isEmpty() || passwordstr.isEmpty()) {
                Toast.makeText(this, "Kérlek add meg az e-mail címet és jelszót!", Toast.LENGTH_SHORT).show();
                return;
            }


        mAuth.signInWithEmailAndPassword(userNamestr, passwordstr)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        OneTimeWorkRequest testWorkRequest = new OneTimeWorkRequest.Builder(WeeklyRecipeWorker.class).build();
                        WorkManager.getInstance(this).enqueue(testWorkRequest);
                        Log.d("MainActivity", "Teszt worker elindítva");

                        Intent intent = new Intent(MainActivity.this, cookBookActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Hibás e-mail vagy jelszó!", Toast.LENGTH_SHORT).show();

                    }
                });

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean hasShownNotification = prefs.getBoolean("shown_notification", false);

        if (!hasShownNotification) {
            OneTimeWorkRequest testWorkRequest = new OneTimeWorkRequest.Builder(WeeklyRecipeWorker.class).build();
            WorkManager.getInstance(this).enqueue(testWorkRequest);
            Log.d("MainActivity", "Teszt worker elindítva");

            prefs.edit().putBoolean("shown_notification", true).apply(); // ne küldje újra


            prefs.edit().putBoolean("shown_notification", false).apply();
        }

    }
    public void register (View view){
        Intent intent = new Intent (MainActivity.this, RegisterActivity.class);
        startActivity(intent);
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