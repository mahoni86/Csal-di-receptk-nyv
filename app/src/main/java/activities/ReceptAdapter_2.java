package activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csaladireceptknyv.R;

import java.util.List;

public class ReceptAdapter_2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_RECEPT = 1;

    private List<Recept> receptLista;
    private Bitmap kepBitmap;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Recept recept);
    }

    public ReceptAdapter_2(List<Recept> receptLista, OnItemClickListener listener) {
        this.receptLista = receptLista;
        this.listener = listener;
        loadImageFromFirestore();
    }

    private void loadImageFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Dokumentum hivatkozás - ezt cseréld le a sajátodra!
        db.collection("receptek")
                .document("aDokumentumId")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String base64String = documentSnapshot.getString("kepBase64");
                        if (base64String != null && !base64String.isEmpty()) {
                            Bitmap bitmap = base64ToBitmap(base64String);
                            setKepBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Hibakezelés, pl. log vagy Toast
                });
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    public void setKepBitmap(Bitmap bitmap) {
        this.kepBitmap = bitmap;
        notifyDataSetChanged();  // újrarenderelés
    }

    @Override
    public int getItemCount() {
        return receptLista.size() + (kepBitmap != null ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (kepBitmap != null && position == 0) ? TYPE_IMAGE : TYPE_RECEPT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recept, parent, false);
            return new ReceptViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(kepBitmap);
        } else {
            int index = kepBitmap != null ? position - 1 : position;
            Recept recept = receptLista.get(index);
            ((ReceptViewHolder) holder).bind(recept, listener);
        }
    }

    public static class ReceptViewHolder extends RecyclerView.ViewHolder {
        TextView nevText;

        public ReceptViewHolder(View itemView) {
            super(itemView);
            nevText = itemView.findViewById(R.id.receptNev);
        }

        public void bind(Recept recept, OnItemClickListener listener) {
            nevText.setText(recept.getNév());
            nevText.setOnClickListener(v -> listener.onItemClick(recept));
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagePreview);
        }

        public void bind(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
