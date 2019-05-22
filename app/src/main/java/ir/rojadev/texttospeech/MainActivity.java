package ir.rojadev.texttospeech;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

/*
 * Developed by Reyhaneh Ezatpanah
 * www.Rojadev.ir
 */

public class MainActivity extends AppCompatActivity
{

    private static final String GOOGLE_TTS_PACKAGE = "com.google.android.tts";
    private static final String TTS_TAG = "TTS";
    private static final String MY_TEXT_UTTERANCE_ID = "MyText";
    EditText edttxt;
    //    Button btnspeak;
    TextToSpeech mTTS;
    SeekBar seekBarPitch;
    SeekBar seekBarSpeed;
    RadioGroup rglanguage;
    RelativeLayout speakBtnContainer;
    ImageView speakImg;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edttxt = findViewById(R.id.editTxt);
        //btnspeak = findViewById(R.id.btnspeak);
        seekBarPitch = findViewById(R.id.seekbrPitch);
        seekBarSpeed = findViewById(R.id.seekbrSpeed);
        rglanguage = findViewById(R.id.rg_language);
        speakBtnContainer = findViewById(R.id.speakBtnContainer);
        speakImg = findViewById(R.id.speakImg);
        progressBar = findViewById(R.id.progressBar);


        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.ERROR)
                {
                    Log.e(TTS_TAG, "Initialize failed");

                    downloadGoohleTTs();
                }
                else
                {
                    if (isPackageInstalled(GOOGLE_TTS_PACKAGE))
                    {
                        mTTS.setEngineByPackageName(GOOGLE_TTS_PACKAGE);
                    }
                    int result = mTTS.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA)
                    {
                        Log.e(TTS_TAG, "Language not supported");
                    }
                    mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener()
                    {
                        @Override
                        public void onStart(String utteranceId)
                        {
                            if (utteranceId.equals(MY_TEXT_UTTERANCE_ID)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        speakImg.setVisibility(View.GONE);
                                        progressBar.setVisibility(View.VISIBLE);
                                        //Toast.makeText(MainActivity.this,"Start to Speech",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onDone(String utteranceId)
                        {
                            if (utteranceId.equals(MY_TEXT_UTTERANCE_ID)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        speakImg.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        //Toast.makeText(MainActivity.this,"Finish to Speech",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onError(String utteranceId)
                        {

                        }
                    });
                }
            }
        });

        speakBtnContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                speak();
            }
        });


    }

    private boolean isPackageInstalled(String googleTtsPackage)
    {
        try
        {
            getPackageManager().getPackageInfo(googleTtsPackage, 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return false;
        }

        return true;
    }

    private void downloadGoohleTTs()
    {
        try
        {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + GOOGLE_TTS_PACKAGE)));
        }
        catch (ActivityNotFoundException anfe)
        {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google" + ".com" + "/store/apps/details?id=" + GOOGLE_TTS_PACKAGE)));
        }
    }


    private void speak()
    {
        String text = edttxt.getText().toString();
        if (text.length() == 0)
        {
            Toast.makeText(this, "text is empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            float pitch = seekBarPitch.getProgress() / 50;
            if (pitch <= 0.1) pitch = 0.1f;
            mTTS.setPitch(pitch);

            float speed = seekBarSpeed.getProgress() / 50;
            if (speed <= 0.1) speed = 0.1f;
            mTTS.setSpeechRate(speed);

            int result;
            int selectedLanguageId = rglanguage.getCheckedRadioButtonId();
            switch (selectedLanguageId)
            {
                default:
                case R.id.rb_english:
                    result = mTTS.setLanguage(Locale.ENGLISH);
                    break;
                case R.id.rb_french:
                    result = mTTS.setLanguage(Locale.FRANCE);
                    break;
                case R.id.rb_swedish:
                    result = mTTS.setLanguage(new Locale("sv", "SE"));
                    break;
            }

            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA)
            {
                Toast.makeText(this, "this language not supported", Toast.LENGTH_SHORT).show();
            }
            else
            {
                HashMap<String, String> map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, MY_TEXT_UTTERANCE_ID);

                mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mTTS != null)
        {
            mTTS.stop();
            mTTS.shutdown();
        }
    }
}
