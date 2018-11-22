package secretbox.alisha.joshua.secretbox;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class startActivity extends AppCompatActivity {
    private Button mReg_btn, mLogin_Btn, bluetooth;
    private ImageView chatBubble;
    private Animation fade;
    Typeface font3;
    private TextView title, welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        /*PrefManager prefManager = new PrefManager(getApplicationContext());
        if(prefManager.isFirstTimeLaunch()){
            prefManager.setFirstTimeLaunch(false);
            startActivity(new Intent(startActivity.this, WelcomeActivity.class));
            finish();
        }
        else{}*/

        title = findViewById(R.id.titleText);
        font3 = Typeface.createFromAsset(getAssets(), "font4.ttf");
        title.setTypeface(font3);

        mReg_btn = findViewById(R.id.reg_btn);
        mLogin_Btn = findViewById(R.id.start_login_btn);
        chatBubble = findViewById(R.id.chatBubble);
        //welcome = findViewById(R.id.welcome);
        fade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        bluetooth = findViewById(R.id.chatBluetooth);

        //welcome.setVisibility(View.INVISIBLE);
        chatBubble.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //welcome.setVisibility(View.VISIBLE);
                chatBubble.setVisibility(View.VISIBLE);
                chatBubble.startAnimation(fade);
                //welcome.startAnimation(fade);
            }
        }, 1000);


        //opens registration activity
        mReg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent = new Intent(startActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

        //login button opens login activity
        mLogin_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mLoginAct = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(mLoginAct);
            }
        });

        //opens activity to chat through bluetooth
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bluetoothIntent = new Intent(getApplicationContext(), BluetoothActivity.class);
                startActivity(bluetoothIntent);
            }
        });

        final int[] t = {0};
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                t[0]++;
                if (t[0] < 20) {
                    if (t[0] % 2 == 0)
                        bluetooth.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.blue));
                    else
                        bluetooth.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.white));
                    h.postDelayed(this, 500);
                }
            }
        }, 1000);
    }
}
