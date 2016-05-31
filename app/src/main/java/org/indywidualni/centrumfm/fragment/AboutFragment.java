package org.indywidualni.centrumfm.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.util.Miscellany;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AboutFragment extends Fragment {

    @BindView(R.id.aboutRadio) TextView aboutRadio;
    @BindView(R.id.aboutText) TextView aboutText;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        aboutRadio.setMovementMethod(LinkMovementMethod.getInstance());
        aboutRadio.setText(Html.fromHtml(Miscellany.readFromAssets("radio.html")));
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
        aboutText.setText(Html.fromHtml(Miscellany.readFromAssets("about.html")));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}