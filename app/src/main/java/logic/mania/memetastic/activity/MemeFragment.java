/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License of this file: GNU GPLv3 (Commercial upon request)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
#########################################################*/
package logic.mania.memetastic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import logic.mania.memetastic.App;

import logic.mania.memetastic.data.MemeData;
import logic.mania.memetastic.ui.GridDecoration;
import logic.mania.memetastic.ui.MemeItemAdapter;
import logic.mania.memetastic.util.AppCast;
import logic.mania.memetastic.util.AppSettings;
import logic.mania.memetastic.util.ContextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import logic.mania.memetastic_test.R;


public class MemeFragment extends Fragment {
    @BindView(R.id.meme_fragment__recycler_view)
    RecyclerView _recyclerMemeList;

    @BindView(R.id.meme_fragment__list_empty_layout)
    LinearLayout _emptylistLayout;

    @BindView(R.id.meme_fragment__list_empty_text)
    TextView _emptylistText;

    @BindView(R.id.publisherAdView)
    com.google.android.gms.ads.doubleclick.PublisherAdView mPublisherAdView;

    App _app;
    int _tabPos;
    String[] _tagKeys, _tagValues;
    private Unbinder _unbinder;
    private List<MemeData.Image> _imageList;
    private MemeItemAdapter _recyclerMemeAdapter;





    public MemeFragment() {
        // Required empty public constructor
    }

    // newInstance constructor for creating fragment with arguments
    public static MemeFragment newInstance(int pagePos) {
        MemeFragment fragmentFirst = new MemeFragment();
        Bundle args = new Bundle();
        args.putInt("pos", pagePos);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _app = (App) getActivity().getApplication();
        _tabPos = getArguments().getInt("pos");

        _imageList = new ArrayList<>();
    }

    private void reloadAdapter() {
        _tagKeys = getResources().getStringArray(R.array.meme_tags__keys);
        _tagValues = getResources().getStringArray(R.array.meme_tags__titles);
        if (_tabPos >= 0 && _tabPos < _tagKeys.length) {
            _imageList = MemeData.getImagesWithTag(_tagKeys[_tabPos]);
        }

        if (_app.settings.isShuffleTagLists()) {
            Collections.shuffle(_imageList);
        }

        List<MemeData.Image> hiddenImages = new ArrayList<>();
        for (MemeData.Image image : _imageList) {
            if (_app.settings.isHidden(image.fullPath.getAbsolutePath())) {
                hiddenImages.add(image);
            }
        }
        _imageList.removeAll(hiddenImages);
        _recyclerMemeAdapter.setOriginalImageDataList(_imageList);
        _recyclerMemeAdapter.notifyDataSetChanged();
        setRecyclerMemeListAdapter(_recyclerMemeAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_meme, container, false);
        _unbinder = ButterKnife.bind(this, root);

        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        mPublisherAdView.loadAd(adRequest);




        _recyclerMemeList.setHasFixedSize(true);
        _recyclerMemeList.setItemViewCacheSize(_app.settings.getGridColumnCountPortrait() * _app.settings.getGridColumnCountLandscape() * 2);
        _recyclerMemeList.setDrawingCacheEnabled(true);
        _recyclerMemeList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        _recyclerMemeList.addItemDecoration(new GridDecoration(1.7f));

        int a = AppSettings.get().getMemeListViewType();
        if (AppSettings.get().getMemeListViewType() == MemeItemAdapter.VIEW_TYPE__ROWS_WITH_TITLE) {
            RecyclerView.LayoutManager recyclerLinearLayout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            _recyclerMemeList.setLayoutManager(recyclerLinearLayout);
        } else {
            int gridColumns = ContextUtils.get().isInPortraitMode()
                    ? _app.settings.getGridColumnCountPortrait()
                    : _app.settings.getGridColumnCountLandscape();
            RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(getActivity(), gridColumns);

            _recyclerMemeList.setLayoutManager(recyclerGridLayout);
        }

        _emptylistText.setText(getString(R.string.no_custom_templates_description__appspecific, getString(R.string.custom_templates_visual)));
        _recyclerMemeAdapter = new MemeItemAdapter(_imageList, getActivity(), AppSettings.get().getMemeListViewType());
        setRecyclerMemeListAdapter(_recyclerMemeAdapter);



        mPublisherAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        return root;
    }

    private void setRecyclerMemeListAdapter(MemeItemAdapter adapter) {
        adapter.setFilter("");
        _recyclerMemeList.setAdapter(adapter);
        boolean isEmpty = adapter.getItemCount() == 0;
        _emptylistLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        _recyclerMemeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private BroadcastReceiver _localBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AppCast.ASSETS_LOADED.ACTION: {
                    reloadAdapter();
                    return;
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
        reloadAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_localBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_unbinder != null) {
            _unbinder.unbind();
        }
    }



}
