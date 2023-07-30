package com.notesapp.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.notesapp.R;
import com.notesapp.model.NoteModel;

import java.util.List;

public class NoteQueryAdapter extends RecyclerView.Adapter<NoteQueryAdapter.MyViewHolder> {
    private List<NoteModel> noteList;
    Context context;

    QueryNoteListener noteListener;
    public NoteQueryAdapter(List<NoteModel> noteList, Context context,  QueryNoteListener noteListener){
        this.noteList = noteList;
        this.context = context;
        this.noteListener = noteListener;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.note_item, parent, false);
        return new NoteQueryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        NoteModel note = noteList.get(position);
        holder.noteTextView.setText(note.getNoteDesc());
        holder.noteTitleView.setText(note.getNoteTitle());
        CharSequence dateCharSeq = DateFormat.format("EEEE, MMM d, yyyy h:mm:ss a", note.getCreated().toDate());

        holder.dateTextView.setText(dateCharSeq);
        DrawableCrossFadeFactory factory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        RequestOptions options =
                new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.drawable.nopictures);
        Glide.with(context).load(note.getNoteImage()).transition(withCrossFade(factory))
                .apply(options).into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            noteListener.handleNoteClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView noteTextView, noteTitleView, dateTextView;
        MaterialCardView cardView;
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            noteTitleView = itemView.findViewById(R.id.noteTitleView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            cardView = itemView.findViewById(R.id.item_card_view);
            imageView = itemView.findViewById(R.id.noteImage);


        }
    }
    public interface QueryNoteListener {
        void handleNoteClick(NoteModel model);
    }
}
