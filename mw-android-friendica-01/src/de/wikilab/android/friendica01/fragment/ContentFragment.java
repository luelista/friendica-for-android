package de.wikilab.android.friendica01.fragment;
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import de.wikilab.android.friendica01.FragmentParentListener;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

public abstract class ContentFragment extends Fragment {
	private static final String TAG="Friendica/ContentFragment";
	
	protected View myView;
	
	String navigateOrder = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (navigateOrder != null) onNavigate(navigateOrder); navigateOrder = null;
	}
	
	public void navigate(String target) {
		if (!isAdded() || myView == null) {
			navigateOrder = target;
		} else {
			onNavigate(target);
		}
	}
	
	protected abstract void onNavigate(String target);
	
	protected void SendMessage(String message, Object arg1, Object arg2) {
		try{
			((FragmentParentListener)getActivity()).OnFragmentMessage(message, arg1, arg2);
		} catch(Exception ignoreException) {}
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
}
