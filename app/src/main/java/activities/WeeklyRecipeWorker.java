package activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.csaladireceptknyv.ReceptReading;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class WeeklyRecipeWorker extends Worker {

    private static final String CHANNEL_ID = "recipe_channel";

    public WeeklyRecipeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        //showNotification("Ez egy teszt notification");//

        Log.d("WorkerDebug", "doWork() called");

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return Result.failure(); // nem jelentkezett be, ne küldj értesítést
        }

        final List<Recept> recipes = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("receptek")
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    String name = doc.getString("Név");
                                    List<String> hozzavalok = (List<String>) doc.get("hozzavalok");
                                    String leiras = doc.getString("leiras");
                                    String kategoria = doc.getString("kategória");
                                    String base64 = doc.getString("imageBase64");

                                    if (name != null) {
                                        Recept recept = new Recept();
                                        recept.setNév(name);
                                        recept.setHozzavalok(hozzavalok);
                                        recept.setLeiras(leiras);
                                        recept.setKategoria(kategoria);
                                        recept.setImageBase64(base64);
                                        recipes.add(recept);
                                }
                            }
                        }
                    latch.countDown();  // Jelez, hogy befejeződött
                });

        try {
            boolean completed = latch.await(10, TimeUnit.MINUTES); // max 10 mp várakozás
            if (!completed) {
                // timeout, Firestore nem válaszolt időben
                return Result.retry();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }

        if (recipes.isEmpty()) {
            return Result.failure();
        }

        // Véletlenszerű recept kiválasztása
        Random random = new Random();
        Recept selectedRecipe = recipes.get(random.nextInt(recipes.size()));

        showNotification(selectedRecipe);
        //showNotification("Teszt recept"); // teszt
        return Result.success();
    }

    private void showNotification(Recept selectedRecipe) {
        String recipeName = selectedRecipe.getNév();

        Log.d("WeeklyRecipeWorker", "showNotification() called with recipe: " + recipeName); //log
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Csatorna létrehozása Android 8+ esetén
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Recept ajánló csatorna",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        //  Intent a részletes recept activity-re
        Intent intent = new Intent(context, ReceptReading.class);
        intent.putExtra("Recept", selectedRecipe);
        intent.putExtra("origin", "notification");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(new Intent(context, cookBookActivity.class));
        stackBuilder.addNextIntent(intent);

        //  PendingIntent a notificationhöz
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Heti recept ajánló")
                .setContentText("Próbáld ki ezt a receptet: " + recipeName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
