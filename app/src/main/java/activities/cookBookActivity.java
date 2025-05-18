package activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.csaladireceptknyv.R;
import foMenu.sertes;
import foMenu.csirkes;
import foMenu.deszert;
import foMenu.glutenFree;
//import foMenu.sos;

public class cookBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cook_book);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LinearLayout[] menuButtons = new LinearLayout[]{
                findViewById(R.id.menu1),
                findViewById(R.id.menu2),
                findViewById(R.id.menu3),
                findViewById(R.id.menu4),
                //findViewById(R.id.menu5)
        };
        for (LinearLayout menu : menuButtons) {
            menu.setOnClickListener(view -> {
                menu.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            int id = view.getId();
                            Intent intent;
                            if (id == R.id.menu1) {
                                intent = new Intent(cookBookActivity.this, sertes.class);
                            } else if (id == R.id.menu2) {
                                intent = new Intent(cookBookActivity.this, csirkes.class);
                            } else if (id == R.id.menu3) {
                                intent = new Intent(cookBookActivity.this, glutenFree.class);
                            }else if (id == R.id.menu4) {
                                intent = new Intent(cookBookActivity.this, deszert.class);
                            }/*else if (id == R.id.menu5) {
                                intent = new Intent(cookBookActivity.this, sos.class);
                            }*/else {
                                return;
                            }
                                startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            menu.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .start();
                        })
                        .start();
            });
        }
    }
}