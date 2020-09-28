package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import com.applex.utsav.adapters.ReelsAdapter;
import com.applex.utsav.models.ReelsPostModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Objects;

public class ReelsActivity extends AppCompatActivity {

    private ViewPager2 reelsList;
    private String uid, bool;
    private Query query;
    private ReelsAdapter adapter;
    private ArrayList<ReelsPostModel> models;
    private String docID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        models = new ArrayList<>();
        reelsList = findViewById(R.id.recyclerReelsViewAll);

        bool = Objects.requireNonNull(getIntent().getStringExtra("bool"));

        if(getIntent().getStringExtra("uid") != null) {
            uid = getIntent().getStringExtra("uid");
        }

        if(getIntent().getStringExtra("docID") != null) {
            docID = getIntent().getStringExtra("docID");
        }

        query = FirebaseFirestore.getInstance().collection("Reels").whereEqualTo("docID", docID);
        buildRecyclerView();
    }

    private void buildRecyclerView() {

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                ReelsPostModel reelsPostModel = document.toObject(ReelsPostModel.class);
                Objects.requireNonNull(reelsPostModel).setDocID(document.getId());
                models.add(reelsPostModel);
                Query query = null;

                if(bool.matches("1")) {
                    query = FirebaseFirestore.getInstance()
                            .collection("Reels")
                            .orderBy("ts", Query.Direction.DESCENDING)
                            .limit(1)
                            .startAfter(document);
                }
                else if(bool.matches("2")) {
                    query = FirebaseFirestore.getInstance()
                            .collection("Reels")
                            .whereEqualTo("uid", uid)
                            .orderBy("ts", Query.Direction.DESCENDING)
                            .limit(1)
                            .startAfter(document);
                }

                Objects.requireNonNull(query).get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful() && Objects.requireNonNull(task1.getResult()).getDocuments().size() != 0) {
                        DocumentSnapshot document1 = Objects.requireNonNull(task1.getResult()).getDocuments().get(0);
                        ReelsPostModel reelsPostModel1 = document1.toObject(ReelsPostModel.class);
                        Objects.requireNonNull(reelsPostModel1).setDocID(document1.getId());
                        models.add(models.size(), reelsPostModel1);
                    }
                });

                adapter = new ReelsAdapter(ReelsActivity.this, models, reelsList, bool, uid);
                reelsList.setAdapter(adapter);
            }
        });
    }
}