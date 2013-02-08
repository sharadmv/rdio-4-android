package com.sharad.rdio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

@SuppressLint("SetJavaScriptEnabled")
public class Rdio extends Activity {

	private ProgressBar pBar;
	private SeekBar sb;

	public static final String SEEK_SELECTOR = "javascript:alert($('.time').html()+'##'+$('.bar')[0].style.cssText.substring(7,$('.bar')[0].style.cssText.indexOf('%')));";
	WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rdio);
		pBar = (ProgressBar) findViewById(R.id.p_bar);
		WebViewClient yourWebClient = new WebViewClient() {
			// Override page so it's load on my view only
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

		};

		// Get Web view
		mWebView = (WebView) findViewById(R.id.webview); // This
															// is
															// the
															// id
															// you
		// gave
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				String[] split = message.split("##");
				Log.d("seek", message);
				if (split.length >= 2) {
					String time = split[0];
					String width = split[1];
					Log.d("seek", time + ", " + width);
					sb.setProgress((int) Double.parseDouble(width));
					((TextView) findViewById(R.id.time)).setText(time);
				}
				result.confirm();
				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (progress < 100 && pBar.getVisibility() == ProgressBar.GONE) {
					pBar.setVisibility(ProgressBar.VISIBLE);
				}
				pBar.setProgress(progress);
				if (progress == 100) {
					(new SeekThread()).start();
					pBar.setVisibility(ProgressBar.GONE);
				}
			}
		});
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginState(PluginState.ON);
		mWebView.getSettings().setUseWideViewPort(true);
		// if ROM supports Multi-Touch
		mWebView.setWebViewClient(yourWebClient);
		String ua = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
		mWebView.getSettings().setUserAgentString(ua);

		// Load URL
		Log.d("seek", "LOADING");
		mWebView.loadUrl("http://www.rdio.com");
		mWebView.setWebViewClient(yourWebClient);
		mWebView.setVisibility(View.GONE);
		Button click = (Button) findViewById(R.id.click);
		Button next = (Button) findViewById(R.id.next);

		click.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mWebView.loadUrl("javascript:$('.play_pause').click()");
			}

		});
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mWebView.loadUrl("javascript:$('.next').click()");
			}

		});
		sb = (SeekBar) findViewById(R.id.seek);
		sb.setMax(100);
		new SeekThread().start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_rdio, menu);
		return true;
	}

	private class SeekThread extends Thread {
		@Override
		public void run() {
			while (true) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mWebView.loadUrl(SEEK_SELECTOR);
					}
				});
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Log.e("seek", e.getMessage(), e);
				}
			}
		}
	}

}
