package com.jrdcom.jrdlogger;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;
import com.jrdcom.jrdlogger.framework.JRDLoggerManager;
import com.jrdcom.jrdlogger.R;

public class MainActivity extends Activity {

	private static final String TAG = "JrdLogger";
	private ToggleButton mStartStopToggleButton;
	private JRDLoggerManager mManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViews();
		initViews();
		setListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void findViews() {
		this.mStartStopToggleButton = ((ToggleButton) findViewById(R.id.startStopToggleButton));
	}

	private void initViews() {
		mManager = new JRDLoggerManager(this);
	}

	private void setListeners() {
		this.mStartStopToggleButton
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramView) {
						boolean bool1 = false;
						ToggleButton localToggleButton;
						if (paramView instanceof ToggleButton) {
							localToggleButton = (ToggleButton) paramView;
							bool1 = localToggleButton.isChecked();
						}
						if (bool1) {
							Log.e(TAG, "start");
							mManager.startLog(1, "from_ui");
						} else {
							Log.e(TAG, "stop");

						}

					}
				});

	}
}
