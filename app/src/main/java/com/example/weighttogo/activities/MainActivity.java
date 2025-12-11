package com.example.weighttogo.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weighttogo.R;
import com.example.weighttogo.adapters.WeightEntryAdapter;
import com.example.weighttogo.models.WeightEntry;

public class MainActivity extends AppCompatActivity implements WeightEntryAdapter.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onEditClick(WeightEntry entry) {
        // TODO: Implement in GREEN phase
    }

    @Override
    public void onDeleteClick(WeightEntry entry) {
        // TODO: Implement in GREEN phase
    }
}
