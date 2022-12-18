package com.example.stack;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private TextView question,no_indicator;
    private FloatingActionButton bookmark_btn;
    private LinearLayout option_container;
    private Button share_btn,next_btn;
    private int count=0;
    private List<QuestionModel> list;
    private  int position =0;
    private  int score =0;
    private String category;
    private int setNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        // Toolbar toolbar = findViewById(R.id.toolbar);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        question = findViewById(R.id.question);
        no_indicator = findViewById(R.id.no_indicator);
        bookmark_btn = findViewById(R.id.bookmark_btn);
        option_container = findViewById(R.id.options_container);
        share_btn = findViewById(R.id.share_btn);
        next_btn = findViewById(R.id.next_btn);


//        list = new ArrayList<>();
//        list.add(new QuestionModel("question1","a","b","c","d","a"));
//        list.add(new QuestionModel("question2","a","b","c","d","c"));
//        list.add(new QuestionModel("question3","a","b","c","d","b"));
//        list.add(new QuestionModel("question4","a","b","c","d","a"));
//        list.add(new QuestionModel("question5","a","b","c","d","a"));
//        list.add(new QuestionModel("question6","a","b","c","d","c"));
//        list.add(new QuestionModel("question7","a","b","c","d","a"));
//        list.add(new QuestionModel("question8","a","b","c","d","d"));
//        list.add(new QuestionModel("question9","a","b","c","d","a"));
//        list.add(new QuestionModel("question10","a","b","c","d","b"));



        category = getIntent().getStringExtra("category");
        setNo = getIntent().getIntExtra("setNo", 1);

        list = new ArrayList<>();

        myRef.child("SETS").child("category").child("questions").orderByChild("setNo").equalTo(setNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    list.add(snapshot.getValue(QuestionModel.class));
                }
                if (list.size() >0) {
                    for (int i = 0; i < 4; i++) {
                        option_container.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {
                                checkAnswer((Button) v);
                            }
                        });
                    }



                    playAnim(question, 0, list.get(position).getQuestion());
                    next_btn.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(View v) {
                            next_btn.setEnabled(false);
                            next_btn.setAlpha(0.7f);
                            position++;
                            enableOption(true);
                            if (position == list.size()) {
                                return;////score activity
                            }
                            count = 0;
                            playAnim(question, 0, list.get(position).getQuestion());

                           }
                    });
//                    for (int i = 0; i < 4; i++) {
//                        option_container.getChildAt(i).setOnClickListener(new View.OnClickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//                            @Override
//                            public void onClick(View v) {
//                                checkAnswer((Button) v);
//                            }
//                        });
//                    }
                }else {
                    finish();
                    Toast.makeText(QuestionsActivity.this,"no questions",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionsActivity.this, databaseError.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void playAnim(final View view,final int value,String data){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                if(value == 0 && count < 4 ){
                    String option = "";
                    if(count == 0){
                        option = list.get(position).getOptionA();
                    }else if(count == 1){
                        option = list.get(position).getOptionB();
                    }else if(count == 2){
                        option = list.get(position).getOptionC();
                    }
                    else if(count == 3){
                        option = list.get(position).getOptionD();
                    }

                    playAnim(option_container.getChildAt(count),0,option);
                    count++;
                }

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if(value == 0){
                    try{
                        ((TextView) view).setText(data);
                        no_indicator.setText(position+1+"/"+list.size());
                    }catch (ClassCastException ex){
                        ((Button) view).setText(data);
                    }
                    view.setTag(data);
                    playAnim(view,1,data);
                }

            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkAnswer(Button selectedoption){
        enableOption(false);
        next_btn.setEnabled(true);
        next_btn.setAlpha(1);
        if(selectedoption.getText().toString().equals(list.get(position).getCorrectANS())){
            //correct
            score++;
            selectedoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

        }else {
            //incorrect
            selectedoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            Button correctoption = (Button) option_container.findViewWithTag(list.get(position).getCorrectANS());
            correctoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void enableOption(boolean enable){
        for(int i=0;i<4;i++){
            option_container.getChildAt(i).setEnabled(enable);
//            if(enable){
//                option_container.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
//            }
        }
    }
}