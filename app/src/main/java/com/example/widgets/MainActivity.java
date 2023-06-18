package com.example.widgets;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button btn;
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextTextPersonName);
        btn = findViewById(R.id.button);

        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        CheckLogin();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = ""+editText.getText();


                if (text.equals("her")){
                    if (shp == null)
                        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);

                    shpEditor = shp.edit();
                    shpEditor.putString("name", text);
                    shpEditor.commit();

                    Intent i = new Intent(MainActivity.this, Dashboard.class);
                    startActivity(i);
                    finish();

                }else if(text.equals("him")){
                    if (shp == null)
                        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);

                    shpEditor = shp.edit();
                    shpEditor.putString("name", text);
                    shpEditor.commit();

                    Intent i = new Intent(MainActivity.this, Dashboard.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Error: invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void CheckLogin() {
        if (shp == null)
            shp = getSharedPreferences("myPreferences", MODE_PRIVATE);

        String userName = shp.getString("name", "");

        if (userName != null && !userName.equals("")) {
            Intent i = new Intent(MainActivity.this, Dashboard.class);
            startActivity(i);
            finish();
        }
    }


}