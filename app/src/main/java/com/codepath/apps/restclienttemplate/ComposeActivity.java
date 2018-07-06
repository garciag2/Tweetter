package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.wafflecopter.charcounttextview.CharCountTextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    ImageView ivProfileImage;
    TextView tvUsername;
    Button tvSend;
    Button tvCancel;
    EditText etBody;
    Context context;

    public TwitterClient client;
    private Tweet replyTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        client = TwitterApp.getRestClient(this);
        context = this;

        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvSend = (Button) findViewById(R.id.tvSend);
        tvCancel = (Button) findViewById(R.id.tvCancel);
        etBody = (EditText) findViewById(R.id.etBody);

        if(getIntent().hasExtra(Tweet.class.getName())) {
            replyTo = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getName()));
            etBody.setText("@" + replyTo.getUser().getScreenName() + ": ");
            etBody.setSelection(etBody.getText().length());
        }

        CharCountTextView tvCharCount = (CharCountTextView) findViewById(R.id.tvTextCounter);
        EditText simpleEditText = (EditText) findViewById(R.id.etBody);

        tvCharCount.setEditText(simpleEditText);
        tvCharCount.setMaxCharacters(140); //Will default to 150 anyway (Twitter emulation)
        tvCharCount.setExceededTextColor(Color.RED); //Will default to red also
        tvCharCount.setCharCountChangedListener(new CharCountTextView.CharCountChangedListener() {
            @Override
            public void onCountChanged(int countRemaining, boolean hasExceededLimit) {
                if (hasExceededLimit) {
                    tvSend.setEnabled(false);
                } else {
                    tvSend.setEnabled(true);
                }
            }
        });

        User.getCurrentUser(this, new User.UserCallbackInterface() {
            @Override
            public void onUserAvailable(User currentUser) {
                Glide.with(context).load(currentUser.getProfileImageUrl()).into(ivProfileImage);
            }
        });



    }

    public void submitTweet(View view){
        EditText simpleEditText = (EditText) findViewById(R.id.etBody);
        String strValue = simpleEditText.getText().toString();

        if(replyTo == null) {
            client.sendTweet(strValue, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                        Intent data = new Intent();
                        data.putExtra("tweet", Parcels.wrap(tweet));
                        setResult(RESULT_OK,data);
                        finish();

                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            });

        } else {
            client.replyTweet(strValue, replyTo.uid, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                        Intent data = new Intent();
                        data.putExtra("tweet", Parcels.wrap(tweet));
                        setResult(RESULT_OK,data);
                        finish();

                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }


    }

    public void cancelTweet (View view) {
        Intent intent = new Intent(this,TimelineActivity.class);
        startActivityForResult(intent, 25);
    }


}
