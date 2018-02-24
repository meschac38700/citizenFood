package fr.citizenfood.citizenfood.Views;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by William on 23/02/2018.
 */

public class BaseActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;

    public static void effetAuClic(View button){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_BUTTON_PRESS:
                        v.getBackground().setColorFilter(0xe040B3E5, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        v.getBackground().setColorFilter(0xe040B3E5, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;

                    case MotionEvent.ACTION_UP:
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;

                }
                return false;
            }
        });
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }



}
