package se.kth.nylun.kvarnspel;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class KvarnspelPreferenceActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
	}
	
}
    