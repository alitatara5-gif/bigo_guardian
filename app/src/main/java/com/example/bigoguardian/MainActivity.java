package com.example.bigoguardian;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.Manifest;
import java.util.ArrayList;

public class MainActivity extends Activity {
    ArrayList<RecordModel> list = new ArrayList<>();
    BaseAdapter adapter;
    int counter = 0;
    RecorderService mService;
    boolean mBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName n, IBinder s) {
            mService = ((RecorderService.LocalBinder) s).getService();
            mBound = true;
        }
        @Override public void onServiceDisconnected(ComponentName n) { mBound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // --- PROSES NODONG IZIN ---
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 
                Manifest.permission.READ_EXTERNAL_STORAGE
            }, 101);
        }

        bindService(new Intent(this, RecorderService.class), connection, Context.BIND_AUTO_CREATE);

        EditText input = findViewById(R.id.inputUrl);
        ListView listView = findViewById(R.id.listRecordings);

        adapter = new BaseAdapter() {
            @Override public int getCount() { return list.size(); }
            @Override public Object getItem(int i) { return list.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup p) {
                if (v == null) v = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, p, false);
                RecordModel m = list.get(i);
                ((TextView)v.findViewById(R.id.txtUrl)).setText(m.url);
                long curDur = mBound ? mService.getNativeDuration(m.id) : 0;
                ((TextView)v.findViewById(R.id.txtDuration)).setText("Durasi: " + getDurationStr(curDur));
                
                v.findViewById(R.id.btnStop).setOnClickListener(view -> {
                    if (mBound) mService.stopNativeRecording(m.id);
                    list.remove(i);
                    notifyDataSetChanged();
                });
                return v;
            }
        };

        listView.setAdapter(adapter);
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            String url = input.getText().toString();
            if (!url.isEmpty()) {
                RecordModel m = new RecordModel(counter++, url);
                list.add(m);
                Intent it = new Intent(this, RecorderService.class);
                it.putExtra("id", m.id);
                it.putExtra("url", url);
                startService(it);
                adapter.notifyDataSetChanged();
                input.setText("");
            }
        });

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override public void run() {
                adapter.notifyDataSetChanged();
                h.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private String getDurationStr(long sec) {
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    class RecordModel {
        int id; String url;
        RecordModel(int id, String url) { this.id = id; this.url = url; }
    }
}
