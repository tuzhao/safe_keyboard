package org.tuzhao.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import org.tuzhao.demo.keyboard.R;
import org.tuzhao.keyboard.view.SafeKeyboard;
import org.tuzhao.keyboard.wiget.SecurityEditText;


public class DemoMainActivity extends AppCompatActivity {

    private SafeKeyboard securityKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lazy);
        LinearLayout parent = findViewById(R.id.login_layout);
        SecurityEditText et = parent.findViewById(R.id.login_input_password);
        securityKeyboard = new SafeKeyboard(this, et);
    }
}
