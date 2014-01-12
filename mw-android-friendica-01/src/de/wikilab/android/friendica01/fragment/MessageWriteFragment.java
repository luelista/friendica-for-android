package de.wikilab.android.friendica01.fragment;

import de.wikilab.android.friendica01.FragmentParentListener;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.TwAjax;
import de.wikilab.android.friendica01.R.id;
import de.wikilab.android.friendica01.R.layout;
import de.wikilab.android.friendica01.R.string;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class MessageWriteFragment extends Fragment {
	
	private static final String TAG="Friendica/MessageWriteFragment";
	
	String replyToId;
	
	Button sendBtn;
	EditText txtSubject,txtRecipient,txtMessage;
	
	private View myView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		myView = inflater.inflate(R.layout.writemessage, container, false);
		
		txtRecipient = (EditText) myView.findViewById(R.id.userNameRecipient);
		txtSubject = (EditText) myView.findViewById(R.id.subject);
		txtMessage = (EditText) myView.findViewById(R.id.maintb);
		
		//if ()
		
        sendBtn = (Button) myView.findViewById(R.id.btn_upload);
        sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
        
		return myView;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		//detachLocationListener();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		((FragmentParentListener)activity).OnFragmentMessage("Set Header Text", getString(R.string.mm_updatemystatus), null);
	}
	
	private void sendMessage() {
		final ProgressDialog pd = ProgressDialog.show(getActivity(), "Posting status...", "Please wait", true, false);
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.addPostData("screen_name", txtRecipient.getText().toString());
		t.addPostData("title", txtSubject.getText().toString());
		t.addPostData("text", txtMessage.getText().toString());
		if (replyToId != null) t.addPostData("replyto", replyToId);
		t.addPostData("source", "<a href='http://andfrnd.wikilab.de'>Friendica for Android</a>");
		
		t.postData(Max.getServer(getActivity()) + "/api/direct_messages/new", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				//getActivity().finish();
				((FragmentParentListener)getActivity()).OnFragmentMessage("Finished", null, null);
			}
		});
	}
}
