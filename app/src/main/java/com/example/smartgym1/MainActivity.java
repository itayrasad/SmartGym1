package com.example.smartgym1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.smartgym1.FBref.refAuth;
import static com.example.smartgym1.FBref.refPlans;
import static com.example.smartgym1.FBref.refUsers;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    User user = new User();
    ListView planListView;

    ArrayList<String>  planList = new ArrayList<String>();

    ArrayAdapter<String> adapter_listView;

    ArrayList<String> exerciseList = new ArrayList<String>();

    ArrayAdapter<String> adapter_alert;

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        planListView = (ListView) findViewById(R.id.planListView);



        final ProgressDialog progressDialog = ProgressDialog.show(this, "Login",
                "Connecting...", true);
        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refUsers.child(firebaseUser.getEmail().replace("."," "))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.copyUser(dataSnapshot.getValue(User.class));
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Connected to " + user.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refPlans.child("Full Body Workout").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                planList.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    planList.add(childSnapShot.getKey());
                }

                adapter_listView = new ArrayAdapter<String>(MainActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, planList);
                planListView.setAdapter(adapter_listView);
                planListView.setOnItemClickListener(MainActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String str = item.getTitle().toString();
        Intent t;
        if(str.equals("profile")){
            t = new Intent(this,ProfileActivity.class);
            startActivity(t);
        }
        if(str.equals("log out")){
            refAuth.signOut();
            SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("stayConnect", false);
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginOrRegisterActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String machineCode = new String(message.getRecords()[0].getPayload());
            Toast.makeText(this, machineCode, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        refPlans.child("Full Body Workout").child(planList.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                exerciseList.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    exerciseList.add(childSnapShot.getValue(String.class));
                }
                adapter_alert = new ArrayAdapter<>(MainActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, exerciseList);
                ListView listView = new ListView(MainActivity.this);
                listView.setAdapter(adapter_alert);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(exerciseList.get(position));
                        builder.setMessage("Please scan the NFC tag from the machine:");
                        builder.setPositiveButton("Start NFC", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                nfcAdapter = null;
                            }
                        });

                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                nfcAdapter = null;
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
                                if (nfcAdapter != null && nfcAdapter.isEnabled() && nfcAdapter.isNdefPushEnabled()){
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, new Intent(MainActivity.this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                                    nfcAdapter.enableForegroundDispatch(MainActivity.this, pendingIntent, null, null);

                                }
                                else {
                                    Toast.makeText(MainActivity.this, "NFC or Android Beam is not active :(", Toast.LENGTH_SHORT).show();
                                    final AlertDialog.Builder builder;
                                    builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Open NFC settings");
                                    builder.setMessage("Press OPEN to open NFC settings:");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                                            dialog.dismiss();
                                        }
                                    });

                                    builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                            dialog.cancel();
                                        }
                                    });



                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                        });
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(planList.get(position));
                builder.setView(listView);
                builder.setCancelable(true);

                AlertDialog AlbertDialog = builder.create();
                AlbertDialog.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}


