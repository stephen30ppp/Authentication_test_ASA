package com.example.NoteSquad_TestApp;

import static android.content.ContentValues.TAG;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class homePageFragment extends Fragment {
    Button logoutButton;
    Button EditScheduleButton;
    private SearchBar searchBar;
    private SearchView searchView;
    private FirebaseFirestore Firestore;
    private ListView StudySchedulelistView;
    private String selectedUsername;
    private ListView listView;
    List <Map<String,Object>> DataList = new ArrayList<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_home_page, container, false);


        //INSTANTIATE FIRESTORE
        Firestore=FirebaseFirestore.getInstance();

        //BUTTON OBJECTS
         logoutButton = (Button) view.findViewById(R.id.logoutButton);
         listView = view.findViewById(R.id.SearchListView);
         EditScheduleButton = view.findViewById(R.id.editScheduleButton);

         //OPEN EDIT SCHEDULE BUTTON
         EditScheduleButton.setOnClickListener(v->{
             HomePage activity = (HomePage) getActivity();
             if(activity!=null){
                 activity.replaceFragment(new studyScheduleUploadFragment());
             }
         });


        //OPEN ACTIVITY
        logoutButton.setOnClickListener(v -> {
            HomePage activity= (HomePage) getActivity();
            if(activity!=null){
                activity.GoogleSignOut();
            }
        });

        searchBar=(SearchBar) view.findViewById(R.id.search_bar);
        searchView=(SearchView)view.findViewById(R.id.search_view);


                    searchBar.setText(searchView.getText());
                    CharSequence searchText = searchView.getText();
                    searchView.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                        @Override
                        public void afterTextChanged(Editable editable) {
                            String searchText = editable.toString();
                            performSearch(searchText, view);

                        }
                    });


        listView.setOnItemClickListener((AdapterView<?> adapterView, View clickedView, int position, long id) -> {
            selectedUsername = (String) adapterView.getItemAtPosition(position);
            Log.d("SelectedUsername", "Username " + selectedUsername);

            findEmailusingUsername(selectedUsername, new OnEmailFoundListener() {
                @Override
                public void onEmailFound(String email) {
                    Log.d("EmailUser", "Email of user " + email);
                    // Call any other methods that need the email
                    if (getActivity() instanceof HomePage) {
                        if(MainActivity.getEmailString().equals(email)){
                            ((HomePage) getActivity()).replaceFragment(new profileFragment());
                        }else{
                            ((HomePage) getActivity()).replaceFragment(new visitProfileFragment(email, MainActivity.getEmailString()));
                        }
                    }
                }
            });
        });




        //SETTING LISTVIEW FOR STUDY SCHEDULE FOR HOMEPAGE
        StudySchedulelistView = (ListView) view.findViewById(R.id.ScheduleListView);
        handleQueryResults(new HandleQueryResultsCallback() {
            @Override
            public void onHandleQueryResults(List<Map<String, Object>> dataList) {
                String[] from = {"Subject","Description", "Venue","timestampField","Author","Study-Mode"};
                int[] to = {R.id.schedule_title, R.id.schedule_description,R.id.schedule_venue,R.id.Schedule_date ,R.id.Schedule_Author, R.id.Study_Mode};
                SimpleAdapter adapter = new SimpleAdapter(
                        getContext(),
                        dataList,
                        R.layout.schedule_list_item,
                        from,
                        to
                );



        //SETTING LISTVIEW FOR STUDY SCHEDULE FOR HOMEPAGE
        StudySchedulelistView = (ListView) view.findViewById(R.id.ScheduleListView);
        handleQueryResults(new HandleQueryResultsCallback() {
            @Override
            public void onHandleQueryResults(List<Map<String, Object>> dataList) {
                String[] from = {"Subject","Description", "Venue","timestampField","Author","Study-Mode"};
                int[] to = {R.id.schedule_title, R.id.schedule_description,R.id.schedule_venue,R.id.Schedule_date ,R.id.Schedule_Author, R.id.Study_Mode};
                SimpleAdapter adapter = new SimpleAdapter(
                        getContext(),
                        dataList,
                        R.layout.schedule_list_item,
                        from,
                        to
                );




                        ;
                Log.d("Study-Schedule", "Successfully ran handleQueryResults()");
                Log.d("Study-Schedule", "Hashmap:"+dataList);

                StudySchedulelistView.setAdapter(adapter);
            }
        });

                        ;
                Log.d("Study-Schedule", "Successfully ran handleQueryResults()");
                Log.d("Study-Schedule", "Hashmap:"+dataList);

                StudySchedulelistView.setAdapter(adapter);
            }
        });





        return view;
    }

    //RETURN EMAIL FROM DATABASE


    public interface OnEmailFoundListener {
        void onEmailFound(String email);
    }




    private interface HandleQueryResultsCallback { void onHandleQueryResults(List<Map<String, Object>> dataList);}
    private void handleQueryResults(HandleQueryResultsCallback callback) {
        // Query the latest 5 documents
        Query query = Firestore.collection("ScheduleList").orderBy("timestampField", Query.Direction.DESCENDING).limit(5);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Map<String, Object>> dataList = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> data = document.getData();
                    try {
                        dataList.add(ChangeDocumentDate(data));


                    } catch (Exception e) {
                        Log.e("Study-Schedule", "Can't add hashmap to list", e);
                    }
                }
                // Call the callback with the result
                callback.onHandleQueryResults(dataList);
                Log.d("Study-Schedule", "Successfully sent hashamp to handleQueryResults()");
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    Log.e("Study-Schedule", "Error in handleQueryResults", exception);
                }
            }
        });
    }


    public Map<String, Object> ChangeDocumentDate(Map <String,Object> map){

        for(Map.Entry<String,Object> iterate: map.entrySet()){
            String keyName= iterate.getKey();

            if(keyName.equals("timestampField")){
                Object keyValues = iterate.getValue();

                if (keyValues instanceof Timestamp) {
                    Timestamp timestamp = (Timestamp) keyValues;
                    Date date = timestamp.toDate();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String formattedDate = dateFormat.format(date);
                    map.replace("timestampField",formattedDate);
                    return map;
                }
            }
        }
    return map;
    }




    public void findEmailusingUsername(String value, OnEmailFoundListener listener) {
        CollectionReference usersRef = Firestore.collection("Users");
        Query query = usersRef.whereEqualTo("username", value);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String emailStringDocID = document.getId();
                    // Do something with the document ID, e.g., store it or display it
                    if (emailStringDocID != null) {
                        // Perform actions with the document ID
                        // For example, log it or update UI
                        Log.d(TAG, "Document ID found: " + emailStringDocID);
                        // Call the listener with the found email
                        listener.onEmailFound(emailStringDocID);
                        return; // Exit the loop if the email is found
                    }
                }
            } else {
                // Handle errors
                Log.e(TAG, "Error getting documents: ", task.getException());
            }
            // Call the listener with null if the email is not found or an error occurs
            listener.onEmailFound(null);
        });
    }



    private void performSearch(String searchText, View view) {
        CollectionReference usersRef = Firestore.collection("Users");
        String searchTerms=searchText.toLowerCase();

        //Query query = usersRef.whereArrayContains("username", searchText);
        Query query = usersRef.whereGreaterThanOrEqualTo("username", searchText)
                .whereLessThan("username", searchText + "\uf8ff");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Map<String, Object>> searchResults = new ArrayList<>();  // Directly work with Map

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> userData = document.getData();
                    searchResults.add(userData);
                }
                Log.d("PerformSearch", "Successfully performSearch");

                updateSearchResultUI(searchResults, view);

            } else {
                // Handle errors
                Log.e(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    public void updateSearchResultUI(List<Map<String, Object>> searchResults, View view) {


        // Create a list of strings to store the usernames
        List<String> usernames = new ArrayList<>();

        // Iterate through each search result map
        for (Map<String, Object> result : searchResults) {
            // Assuming "username" is the key for the username in the map
            String username = (String) result.get("username");

            // Add the username to the list
            if (username != null) {
                usernames.add(username);
            }
        }
        // Create an ArrayAdapter to bind the data to the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, usernames);

        // Set the adapter to the ListView
        listView.setAdapter(adapter);











    }






















}




