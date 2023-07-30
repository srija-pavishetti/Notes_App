package com.notesapp;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.card.MaterialCardView;
import com.notesapp.model.NoteModel;
import com.notesapp.model.NoteModelSer;
import com.notesapp.prefs.PreffConst;

import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {

    NoteModelSer noteModel;
    TextView noteTextView, noteTitleView;
    ImageView imageView;
    TextToSpeech t1;

    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        getSupportActionBar().setTitle("Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            noteModel = (NoteModelSer) getIntent().getSerializableExtra(PreffConst.NoteData);
            initViews();
            bindViews();
            initTTS();
        }
    }

    private void initTTS() {
        t1 = new TextToSpeech(getApplicationContext(), status -> {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.UK);
            }
        });
    }

    //create bitmap from the view
    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.desc_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tts) {
            textToSpeech();
            return true;
        }else if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }else if (item.getItemId() == R.id.shareNote){
            shareNote(getBitmapFromView(scrollView,scrollView.getChildAt(0).getHeight(),
                    scrollView.getChildAt(0).getWidth()));
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareNote(Bitmap bitmap) {

        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
        Uri bitmapUri = Uri.parse(bitmapPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share Note"));
    }

    private void textToSpeech() {

        Toast.makeText(getApplicationContext(), "Description",Toast.LENGTH_SHORT).show();
        t1.speak(noteModel.getNoteDesc(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    private void initViews() {
        noteTextView = findViewById(R.id.noteTextView);
        noteTitleView = findViewById(R.id.noteTitleView);
        imageView = findViewById(R.id.noteImage);
        scrollView = findViewById(R.id.scrollView);
    }

    private void bindViews() {
        noteTextView.setText("Description: "+noteModel.getNoteDesc());
        noteTitleView.setText("Title: "+noteModel.getNoteTitle());
        Log.e("TAG",noteModel.getNoteImage());
        if (!noteModel.getNoteImage().equals("")) {
            DrawableCrossFadeFactory factory =
                    new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

            RequestOptions options =
                    new RequestOptions()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.drawable.nopictures);
            Glide.with(NoteDetailActivity.this).load(noteModel.getNoteImage()).transition(withCrossFade(factory))
                    .apply(options).into(imageView);
        }else {
            imageView.setVisibility(View.GONE);
        }
    }
    
}