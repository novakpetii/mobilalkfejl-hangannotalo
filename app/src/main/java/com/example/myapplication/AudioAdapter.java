package com.example.myapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    private List<AudioFile> audioFileList;
    private Context context;
    private FirestoreDatabaseHelper dbHelper;
    private int lastAnimatedPosition = -1;

    public AudioAdapter(Context context, List<AudioFile> audioFileList, FirestoreDatabaseHelper dbHelper) {
        this.context = context;
        this.audioFileList = audioFileList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioFile file = audioFileList.get(position);

        holder.textName.setText(file.getFileName());
        holder.textTag.setText("Címke: " + file.getTag());
        holder.textSize.setText("Méret: " + formatSize(file.getSize()));
        holder.textDuration.setText("Hossz: " + file.getDuration() + " s");
        holder.textFileType.setText("Típus: " + formatFileType(file.getFileType()));

        // fade in anim
        if (position > lastAnimatedPosition) {
            Animation fadeIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
            holder.itemView.startAnimation(fadeIn);
            lastAnimatedPosition = position;
        }

        // play (nem mukodik,nincs implementalva)
        holder.buttonPlay.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse(file.getFilePath());
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, uri);
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Nem sikerült lejátszani a fájlt", Toast.LENGTH_SHORT).show();
            }
        });

        // cimke szerk
        holder.buttonEdit.setOnClickListener(v -> {
            EditText editText = new EditText(context);
            editText.setText(file.getTag());
            new AlertDialog.Builder(context)
                    .setTitle("Címke szerkesztése")
                    .setView(editText)
                    .setPositiveButton("Mentés", (dialog, which) -> {
                        String newTag = editText.getText().toString();
                        dbHelper.updateTag(file.getId(), newTag,
                                aVoid -> {
                                    file.setTag(newTag);
                                    holder.textTag.setText("Címke: " + newTag);
                                    Toast.makeText(context, "Címke frissítve", Toast.LENGTH_SHORT).show();
                                },
                                e -> {
                                    Toast.makeText(context, "Hiba a címke frissítésekor", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                });
                    })
                    .setNegativeButton("Mégse", null)
                    .show();
        });

        // file torles
        holder.buttonDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dbHelper.deleteFile(file.getId(),
                                aVoid -> {
                                    audioFileList.remove(pos);
                                    notifyItemRemoved(pos);
                                    Toast.makeText(context, "Fájl törölve", Toast.LENGTH_SHORT).show();
                                },
                                e -> {
                                    Toast.makeText(context, "Hiba a fájl törlésekor", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                holder.itemView.startAnimation(fadeOut);
            } else {
                Toast.makeText(context, "Hiba: Érvénytelen pozíció", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatSize(long sizeInBytes) {
        double sizeInMB = (double) sizeInBytes / (1024 * 1024);
        return String.format("%.2f MB", sizeInMB);
    }

    private String formatFileType(String type) {
        if (type == null) {
            return "unknown";
        }
        type = type.toLowerCase();
        if (type.contains("mpeg")) {
            return "mp3";
        } else if (type.contains("wav")) {
            return "wav";
        }
        return type;
    }

    @Override
    public int getItemCount() {
        return audioFileList.size();
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textTag, textSize, textDuration, textFileType;
        Button buttonPlay, buttonEdit, buttonDelete;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textFileName);
            textTag = itemView.findViewById(R.id.textFileTag);
            textSize = itemView.findViewById(R.id.textFileSize);
            textDuration = itemView.findViewById(R.id.textFileDuration);
            textFileType = itemView.findViewById(R.id.textFileType);
            buttonPlay = itemView.findViewById(R.id.buttonPlay);
            buttonEdit = itemView.findViewById(R.id.buttonEditTag);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
