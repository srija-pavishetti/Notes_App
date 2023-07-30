package com.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.notesapp.adapter.NoteQueryAdapter;
import com.notesapp.adapter.NotesRecyclerAdapter;
import com.notesapp.model.NoteModel;
import com.notesapp.model.NoteModelSer;
import com.notesapp.prefs.PreffConst;
import com.notesapp.utils.ImageUtils;
import com.notesapp.utils.NetworkUtils;
import com.notesapp.utils.toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        NotesRecyclerAdapter.NoteListener, NoteQueryAdapter.QueryNoteListener {
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "addNote";

    RecyclerView recyclerView;
    NotesRecyclerAdapter notesRecyclerAdapter;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    public static final int IMAGE_REQ=20032;
    int select_image;
    EditText noteDescription;
    ImageView noteImage;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    AppCompatEditText search_edit_text;
    ImageView voice_search_query;
    int from;
    String imageurl;
    FloatingActionButton refreshfloatingActionButton;
    StringWriter sw;

    private MediaRecorder mRecorder;
    // string variable is created for storing a file name
    private static String mFileName = null;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Home");
        firebaseAuth= FirebaseAuth.getInstance();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        sw = new StringWriter();
        initView();
        userId = firebaseAuth.getCurrentUser().getUid();
        Log.e(TAG,"FF"+firebaseAuth.getCurrentUser().getUid());
    }

    private void initView() {
        findViewById(R.id.floatingActionButton).setOnClickListener(view -> {
            uploadNoteDialog();
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        search_edit_text = findViewById(R.id.search_edit_text);
        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
              //  initRecyclerView(firebaseAuth.getCurrentUser());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                CharSequence searchText = charSequence.toString().trim();
                searchDb(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
              //  initRecyclerView(firebaseAuth.getCurrentUser());
            }
        });

        voice_search_query = findViewById(R.id.voice_search_query);
        voice_search_query.setOnClickListener(v->{
                from=1;
            speechToText(R.string.speakToSearchNote);
        });

        refreshfloatingActionButton = findViewById(R.id.refreshfloatingActionButton);
        refreshfloatingActionButton.setOnClickListener(v-> {
            initRecyclerView(Objects.requireNonNull(firebaseAuth.getCurrentUser()));
        });
    }

    private void searchDb(CharSequence searchText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(PreffConst.COLLECTION_PATH+userId)
                .whereEqualTo(PreffConst.noteTitle,searchText.toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                Log.e(TAG, String.valueOf(list.size()));
                List<NoteModel> noteModel = queryDocumentSnapshots.toObjects(NoteModel.class);
                NoteQueryAdapter adapter = new NoteQueryAdapter(noteModel,HomeActivity.this,this);
                recyclerView.setAdapter(adapter);
                search_edit_text.setText("");
            }
        }).addOnFailureListener(e -> {

        });
    }

    private void uploadNoteDialog() {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.add_notes_dialog, viewGroup, false);
        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

        EditText noteTitle = dialogView.findViewById(R.id.noteTitle);
        noteImage = dialogView.findViewById(R.id.noteImage);
        ImageView tts = dialogView.findViewById(R.id.tts);
        noteDescription = dialogView.findViewById(R.id.noteDescription);
        Button addNoteImage = dialogView.findViewById(R.id.addNoteImage);
        Button uploadNote = dialogView.findViewById(R.id.uploadNote);

        Button addAudioNote = dialogView.findViewById(R.id.addAudioNote);
        Button stopAndSave = dialogView.findViewById(R.id.stopAndSave);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        uploadNote.setOnClickListener(view -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String snoteTitle = noteTitle.getText().toString();
            String snoteDescription = noteDescription.getText().toString();
            if (snoteTitle.equals("") || snoteDescription.equals("")){
                toast.meActivity(HomeActivity.this,"Please add note title and description!");
            }else {
                noteMethod(userId, snoteTitle, snoteDescription, alertDialog);
            }
        });

        tts.setOnClickListener(view -> {
            from=0;
            speechToText(R.string.speech_prompt);
        });

        addNoteImage.setOnClickListener(view -> {
            chooseImage(HomeActivity.this,IMAGE_REQ);
        });

        addAudioNote.setOnClickListener(v->{
            new Handler().post(this::recordAudio);
        });

        stopAndSave.setOnClickListener(v->{
            stopRecording();
        });
    }

    private void stopRecording() {
        mRecorder.stop();

        // below method will release
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
        Log.e(TAG,mFileName);
        toast.meActivity(this,"Recording Stopped");

    }

    private void recordAudio() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
      /*  mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";*/

        File outputFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster/Dub/");
        Log.i(TAG, "startRecording: creating output file " + outputFolder.mkdirs());
        File output = new File(outputFolder.getAbsolutePath()+"out" + new Date().getTime() + ".3gpp");

        mFileName = output.getAbsolutePath();
        // below method is used to initialize
        // the media recorder class
        mRecorder = new MediaRecorder();

        // below method is used to set the audio
        // source which we are using a mic.
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        // below method is used to set
        // the output format of the audio.
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        // below method is used to set the
        // audio encoder for our recorded audio.
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // below method is used to set the
        // output file location for our recorded audio
        mRecorder.setOutputFile(mFileName);
        mRecorder.setMaxDuration(3000);
        try {
            // below method will prepare
            // our audio recorder class
            mRecorder.prepare();
        } catch (IOException e) {
            toast.meActivity(this,"Fail to Record!");
            Log.e("TAG", "prepare() failed");
        }
        // start method will start
        // the audio recording.
        mRecorder.start();
        toast.meActivity(this,"Recording Started");

    }

    private void speechToText(int resId) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getString(resId));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void chooseImage(Context context, int requestCode){
        final CharSequence[] optionsMenu = {"Take Photo","Choose from Gallery", "Exit" }; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, (dialogInterface, i) -> {
            if(optionsMenu[i].equals("Take Photo")){
                // Open the camera and get the photo
                if (NetworkUtils.isNetworkAvailable(getApplicationContext())){
                    select_image=0;
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, requestCode);
                }else {
                    toast.message(getApplicationContext(),"Network not available!");
                }
            }
            else if(optionsMenu[i].equals("Choose from Gallery")){
                // choose from  external storage
                if (NetworkUtils.isNetworkAvailable(getApplicationContext())){
                    select_image=1;
                    Intent intenth = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intenth.setType("image/*");
                    intenth.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );
                    intenth.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intenth , requestCode);
                }else {
                    toast.message(getApplicationContext(),"Network not available!");
                }


            }
            else if (optionsMenu[i].equals("Exit")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    public Uri getImageUri(Bitmap src, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), src, "title", null);
        return Uri.parse(path);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_CODE_SPEECH_INPUT && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(from==0){
                    sw.append(result.get(0)).append(" ");
                    noteDescription.setText(sw.toString());
                }else {
                    search_edit_text.setText(result.get(0));
                }

            }else if (requestCode == IMAGE_REQ) {
                if (select_image==0){
                    Bitmap photo = (Bitmap)data.getExtras().get("data");
                    Glide.with(this).load(photo).into(noteImage);

                    Uri img = getImageUri(photo, Bitmap.CompressFormat.JPEG,50);
                    uploadImage(img);
                }else if (select_image==1) {
                    try {
                        Uri imageUri = data.getData();
                        String imagePath= ImageUtils.getPath(getApplicationContext(), imageUri);
                        Glide.with(this).load(imagePath).into(noteImage);
                        uploadImage(imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
    private void uploadImage(Uri filePath) {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            taskSnapshot -> {
                       // taskSnapshot.getUploadSessionUri().
                                // Image uploaded successfully
                                // Dismiss dialog
                                progressDialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                String myurl  = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                                Log.d(TAG, "image uploading url " + myurl);

                                ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        imageurl=task.getResult().toString();
                                        Log.i("URL",imageurl);
                                    }
                                });
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(
                            (com.google.firebase.storage.OnProgressListener<? super UploadTask.TaskSnapshot>) taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            });
        }
    }

    private void noteMethod(String userId, String snoteTitle, String snoteDescription, AlertDialog alertDialog) {
        NoteModel nt = null;
        if (imageurl!=null) {
            nt = new NoteModel(snoteDescription, snoteTitle, imageurl, false, new Timestamp(new Date()), userId);
            FirebaseFirestore.getInstance()
                    .collection(PreffConst.COLLECTION_PATH+userId)
                    .add(nt)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "onSuccess: Successfully added note");
                        Toast.makeText(HomeActivity.this, "Successfully added note", Toast.LENGTH_SHORT).show();
                        initRecyclerView(Objects.requireNonNull(firebaseAuth.getCurrentUser()));
                        imageurl = "";
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

        }else {
            nt = new NoteModel(snoteDescription, snoteTitle, "", false, new Timestamp(new Date()), userId);
            FirebaseFirestore.getInstance()
                    .collection(PreffConst.COLLECTION_PATH+userId)
                    .add(nt)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "onSuccess: Successfully added note");
                        Toast.makeText(HomeActivity.this, "Successfully added note", Toast.LENGTH_SHORT).show();
                        initRecyclerView(Objects.requireNonNull(firebaseAuth.getCurrentUser()));
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        if(notesRecyclerAdapter != null){
            notesRecyclerAdapter.stopListening();
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            //startLoginActivity();
            return;
        }
        initRecyclerView(firebaseAuth.getCurrentUser());
    }

    public void initRecyclerView(FirebaseUser user){
        Query query = FirebaseFirestore.getInstance()
                .collection(PreffConst.COLLECTION_PATH+userId)
                .whereEqualTo("userId", user.getUid())
                /*.orderBy("completed", Query.Direction.ASCENDING)
                .orderBy("created", Query.Direction.DESCENDING)*/;


        FirestoreRecyclerOptions<NoteModel> options = new FirestoreRecyclerOptions.Builder<NoteModel>()
                .setQuery(query, NoteModel.class)
                .build();

        notesRecyclerAdapter = new NotesRecyclerAdapter(options,this,HomeActivity.this);
        recyclerView.setAdapter(notesRecyclerAdapter);

        notesRecyclerAdapter.startListening();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                Toast.makeText(HomeActivity.this, "Deleting", Toast.LENGTH_SHORT).show();

                NotesRecyclerAdapter.NoteViewHolder noteViewHolder = (NotesRecyclerAdapter.NoteViewHolder) viewHolder;
                noteViewHolder.deletItem();

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX,
                                float dY, int actionState, boolean isCurrentlyActive) {
            /*new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.delete))
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();*/

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    public void handleCheckChanged(boolean isChecked, DocumentSnapshot snapshot) {
        Log.d(TAG, "handleCheckChanged: " + isChecked);
        snapshot.getReference().update("completed", isChecked)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: "))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getLocalizedMessage()));
    }

    @Override
    public void handleEditNote(DocumentSnapshot snapshot) {

        NoteModel note = snapshot.toObject(NoteModel.class);
        NoteModelSer noteModelSer= new NoteModelSer(note.getNoteDesc(),note.getNoteTitle(),note.getNoteImage(),note.isCompleted(),note.getUserId());
        EditText editText = new EditText( this);
        editText.setText(note.getNoteDesc().toString());
        editText.setSelection(note.getNoteDesc().length());
        new AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setView(editText)
                .setPositiveButton("Done", (dialog, which) -> {
                    String newText = editText.getText().toString();
                    note.setNoteDesc(newText);
                    snapshot.getReference().set(note)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Successfully Updated"))
                            .addOnFailureListener(e -> Log.d(TAG, "onFailure: "+ e.getLocalizedMessage()));
                })
                .setNeutralButton("View Note", (dialogInterface, i) -> {
                    startActivity(new Intent(HomeActivity.this,NoteDetailActivity.class)
                            .putExtra(PreffConst.NoteData,noteModelSer));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void handledeleteItem(DocumentSnapshot snapshot) {

        DocumentReference documentReference = snapshot.getReference();
        NoteModel note = snapshot.toObject(NoteModel.class);

        documentReference.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Item Deleted"))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: "+e.getLocalizedMessage()));

        Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> documentReference.set(note))
                .show();
    }

    @Override
    public void handleNoteClick(NoteModel note) {
        NoteModelSer noteModelSer= new NoteModelSer(note.getNoteDesc(),note.getNoteTitle(),note.getNoteImage(),note.isCompleted(),note.getUserId());
        EditText editText = new EditText( this);
        editText.setText(note.getNoteDesc().toString());
        editText.setSelection(note.getNoteDesc().length());
        new AlertDialog.Builder(this)
                .setTitle("Note")
                .setView(editText)
                .setNeutralButton("View Note", (dialogInterface, i) -> {
                    startActivity(new Intent(HomeActivity.this,NoteDetailActivity.class)
                            .putExtra(PreffConst.NoteData,noteModelSer));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}