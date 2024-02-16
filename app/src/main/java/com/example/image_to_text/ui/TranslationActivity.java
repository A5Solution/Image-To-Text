package com.example.image_to_text.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.image_to_text.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TranslationActivity extends AppCompatActivity {
    private EditText sourceLanguage;
    private TextView destinationLanguageTv;
    private MaterialButton sourceLanguageChooseBtn;
    private MaterialButton destinationLanguageChooseBtn;
    private MaterialButton translateBtn;
    private Translator translator;
    private TranslatorOptions translatorOptions;
    private ProgressDialog progressDialog;
    private static final String TAG="MAIN_TAG";

    private String sourceLanguageCode="en";
    private String sourceLanguageTitle="English";
    private String destinationLanguageCode="ur";
    private String destinationLanguageTitle="Urdu";


    private ArrayList<ModelLanguage> languageArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);
        sourceLanguage=findViewById(R.id.sourceLanguage);
        destinationLanguageTv=findViewById(R.id.destinationLanguageTv);
        sourceLanguageChooseBtn=findViewById(R.id.sourceLanguageChooseBtn);
        destinationLanguageChooseBtn=findViewById(R.id.destinationLanguageChooseBtn);
        translateBtn=findViewById(R.id.translateBtn);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        loadAvailableLanguages();
        sourceLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sourceLanguageCode();
            }
        });

        destinationLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                destinationLanguageChoose();
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateData();
            }
        });
    }

    private String sourceLanguageText="";

    private void validateData() {
        sourceLanguageText=sourceLanguage.getText().toString().trim();
        if(sourceLanguageText.isEmpty()){
            Toast.makeText(this, "Enter text to translate...", Toast.LENGTH_SHORT).show();
        }
        else {
            startTranslations();
        }
    }

    private void startTranslations() {
        progressDialog.setMessage("Processing Language model...");
        progressDialog.show();
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguageCode)
                        .setTargetLanguage(destinationLanguageCode)
                        .build();
        translatorOptions = options;
        translator = Translation.getClient(translatorOptions);
        DownloadConditions downloadConditions=new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.setMessage("Translating...");
                        translator.translate(sourceLanguageText)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        progressDialog.dismiss();
                                        destinationLanguageTv.setText(s);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(TranslationActivity.this, "Failed! "+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(TranslationActivity.this, "Failed! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sourceLanguageCode(){
        PopupMenu popupMenu=new PopupMenu(this,sourceLanguageChooseBtn);
        for (int i=0;i<languageArrayList.size();i++){
            popupMenu.getMenu().add(Menu.NONE,i,i,languageArrayList.get(i).languageTitle);

        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int postion=item.getItemId();

                sourceLanguageCode=languageArrayList.get(postion).languageCode;
                sourceLanguageTitle=languageArrayList.get(postion).languageTitle;

                sourceLanguageChooseBtn.setText(sourceLanguageTitle);
                sourceLanguage.setHint("Enter "+sourceLanguageTitle);

                return false;
            }
        });
    }

    private void destinationLanguageChoose(){
        PopupMenu popupMenu=new PopupMenu(this,destinationLanguageChooseBtn);
        for(int i =0;i<languageArrayList.size();i++){
            popupMenu.getMenu().add(Menu.NONE,i,i,languageArrayList.get(i).getLanguageTitle());

        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int position=item.getItemId();
                destinationLanguageCode=languageArrayList.get(position).languageCode;
                destinationLanguageTitle=languageArrayList.get(position).languageTitle;
                destinationLanguageChooseBtn.setText(destinationLanguageTitle);
                return false;
            }
        });
    }
    private void loadAvailableLanguages() {
        languageArrayList=new ArrayList<>();
        List<String> languageCodeList= TranslateLanguage.getAllLanguages();
        for(String languageCode:languageCodeList){
            String languageTitle=new Locale(languageCode).getDisplayLanguage();
            ModelLanguage modelLanguage=new ModelLanguage(languageCode,languageTitle);
            languageArrayList.add(modelLanguage);
        }

    }
}