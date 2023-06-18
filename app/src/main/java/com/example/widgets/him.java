package com.example.widgets;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link him#newInstance} factory method to
 * create an instance of this fragment.
 */
public class him extends Fragment {
    ImageView image;
    TextView text;
    Button button;
    FirebaseAuth mAuth;
    String storagePath = "His_Picture/";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_GALLERY_PICK_CODE = 300;
    private static final int IMAGE_CAMERA_PICK_CODE = 400;
    String[] cameraPermission;
    String[] storagePermission;

    Uri image_uri = null;

    FirebaseDatabase database;
    StorageReference storageReference;

    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;
     String Message;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public him() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment him.
     */
    // TODO: Rename and change types and number of parameters
    public static him newInstance(String param1, String param2) {
        him fragment = new him();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_him, container, false);

        image= v.findViewById(R.id.imageView);
        text = v.findViewById(R.id.text);
        button = v.findViewById(R.id.hisbutton);

        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        mAuth = FirebaseAuth.getInstance();


        updateImage();
        updateText();


        if (shp == null) {
            shp = getActivity().getSharedPreferences("myPreferences", MODE_PRIVATE);
        }else {
            Intent i = new Intent(getActivity(), MainActivity.class);
            startActivity(i);
            getActivity().finish();
        }

        String userName = shp.getString("name", "");

        if (userName.equals("her")){
            button.setVisibility(View.GONE);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
            }
        });
        return v;
    }
    private void showEditDialog() {
        String[] option = {"Edit Image", "Edit Message"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        showImgDialog();

                        break;
                    case 1:
                        showTextDialog();
                        break;
                }
            }
        });
        builder.show();

    }
    private void showTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Message");


        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

            // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message = input.getText().toString();
                uploadMessage(Message);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void uploadMessage(String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, Object> hashmap = new HashMap<>();
        hashmap.put("Message", message);
        reference.child("his").updateChildren(hashmap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                updateText();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    private void updateText() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference1.orderByChild("user").equalTo("his");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String msg = "" + ds.child("Message").getValue();
                    text.setText(msg);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showImgDialog() {
        String[] option = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }
    private boolean checkStoragePermission() {
        boolean result = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storagePermission, STORAGE_REQUEST_CODE);
        checkStoragePermission();
        if (checkCameraPermission()) {
            pickFromGallery();
        }
    }
    private boolean checkCameraPermission() {
        boolean result = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermission, CAMERA_REQUEST_CODE);
        checkCameraPermission();
        if (!checkCameraPermission()) {
            requestCameraPermission();
        } else {
            pickFromCamera();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        Toast.makeText(getActivity(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Please Enable Camera & Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        Toast.makeText(getActivity(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_PICK_CODE) {
                image_uri = data.getData();
                uploadPicture(image_uri);
            }
            if (requestCode == IMAGE_CAMERA_PICK_CODE) {
                uploadPicture(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAMERA_PICK_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_GALLERY_PICK_CODE);
    }

    private void uploadPicture(Uri uri) {
        String filePathAndName = storagePath;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> results = new HashMap<>();
                            results.put("Image", downloadUri);
                            reference.child("his").updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            updateImage();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });
                        } else {
                            Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateImage() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference1.orderByChild("user").equalTo("his");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String img = "" + ds.child("Image").getValue();
                    if (getActivity() != null) {
                        Picasso.get()
                                .load(img)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(image);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(getActivity(), new  OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        updateImage();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }
}