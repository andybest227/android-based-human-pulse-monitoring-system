package com.example.heartbeatratemonitor.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.heartbeatratemonitor.R;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

public class History extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public  static ArrayList<String> history = new ArrayList<>();
    public static ArrayAdapter<String> arrayAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public History() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static History newInstance(String param1, String param2) {
        History fragment = new History();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        populateHistory();
    }
    public void populateHistory(){
        ListView listView = requireActivity().findViewById(R.id.history);
        final SharedPreferences sharedPreferences = requireContext().getSharedPreferences("com.example.heartBeatRate", Context.MODE_PRIVATE);
        //String saved_history = sharedPreferences.getString("history", null);
        HashSet<String> set = (HashSet<String>) sharedPreferences.getStringSet("history", null);
        if(set == null){
            history = new ArrayList<>();
        }else{
            history = new ArrayList<>(set);
        }

        //Using custom listView Provided by Android Studio
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_expandable_list_item_1, history);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            final int itemToDelete = position;

            //To delete the data from the App history
            new AlertDialog.Builder(requireContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Warning!!")
                    .setMessage("Do you want to delete this history?")
                    .setPositiveButton("Yes", (dialog, which)->{
                        history.remove(itemToDelete);
                        arrayAdapter.notifyDataSetChanged();
                        SharedPreferences sharedPreferences1 = requireContext().getSharedPreferences("com.example.heartBeatRate", Context.MODE_PRIVATE);
                        HashSet<String> set1 = new HashSet<>(History.history);
                        sharedPreferences1.edit().putStringSet("history", set1).apply();
                    })
                    .setNegativeButton("No", null).show();
            return true;
        });

    }
}