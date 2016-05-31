package org.indywidualni.centrumfm.activity;

public interface NewsableActivity {
    void playEnclosure(String url);

    void openCustomTab(String url);

    void shareTextUrl(String url, String text);
}
