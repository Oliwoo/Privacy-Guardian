package com.unict.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unict.mobile.adapters.AppWithMicrophoneAccessAdapter;
import com.unict.mobile.adapters.models.ItemAppDetail;
import com.unict.mobile.R;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.MicrophoneUsageUtils;
import com.unict.mobile.utils.ResourcesUtils;
import com.unict.mobile.utils.decorators.VerticalItemDecorator;

import java.util.List;
import java.util.stream.Collectors;

public class AppWithMicrophoneAccessFragment extends BaseFragment {
    private AppWithMicrophoneAccessAdapter adapter;

    public static AppWithMicrophoneAccessFragment newInstance(){
        AppWithMicrophoneAccessFragment fragment = new AppWithMicrophoneAccessFragment();
        fragment.setGoBackTarget(HomeFragment.newInstance());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_with_microphone_access, container, false);
        setTitle(ResourcesUtils.getString(requireContext(),R.string.app_with_microphone_access_title));
        BaseRecyclerViewContainer list = v.findViewById(R.id.fragment_app_with_microphone_access_list);
        adapter = new AppWithMicrophoneAccessAdapter(list);
        list.addItemDecoration(new VerticalItemDecorator(8));
        list.setAdapter(adapter);

        loadData();
        return v;
    }

    private void loadData(){
        AsyncDataLoadHelper.execute(this, null, new AsyncDataLoadHelper.Callback<List<ItemAppDetail>>(){
            @Override
            public List<ItemAppDetail> getDataRoutine(){
                return MicrophoneUsageUtils.getAppsWithMicrophoneAccess(requireContext())
                .stream()
                .map(d -> new ItemAppDetail(d.appName, d.packageName, d.appIcon))
                .collect(Collectors.toList());
            }
            @Override
            public void onResult(List<ItemAppDetail> result) {
                adapter.setData(result);
            }
        });
    }
}