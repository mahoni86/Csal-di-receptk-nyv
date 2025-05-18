package activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csaladireceptknyv.R;

import java.util.List;

public class ReceptAdapter extends RecyclerView.Adapter<ReceptAdapter.ViewHolder> {

    private List<Recept> receptLista;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Recept recept);
    }

    public ReceptAdapter(List<Recept> receptLista, OnItemClickListener listener) {
        this.receptLista = receptLista;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nevText;

        public ViewHolder(View itemView) {
            super(itemView);
            nevText = itemView.findViewById(R.id.receptNev);

        }

        public void bind(Recept recept, OnItemClickListener listener) {
            nevText.setText(recept.getNév());
            nevText.setOnClickListener( v -> {
                listener.onItemClick(recept);
            });

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recept, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recept recept = receptLista.get(position);
        holder.nevText.setText(recept.getNév());
        holder.bind(recept, listener);
    }

    @Override
    public int getItemCount() {
        return receptLista.size();
    }

}
