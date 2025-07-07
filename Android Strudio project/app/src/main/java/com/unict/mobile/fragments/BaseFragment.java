package com.unict.mobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.unict.mobile.utils.ResourcesUtils;

public abstract class BaseFragment extends Fragment{
    public interface OnFragmentChangeEventListener {
        void OnFragmentChangeRequest(BaseFragment fragment);
    }
    public interface TitleChangeListener {
        void onTitleChanged(String title, BaseFragment fragment);
    }

    private String title = "Loading..";
    private BaseFragment goBackTarget = null;

    protected ResourcesUtils resourcesUtils;

    public BaseFragment(){}
    protected OnFragmentChangeEventListener fragmentChangeListener;
    protected TitleChangeListener titleChangeListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentChangeEventListener) {
            fragmentChangeListener = (OnFragmentChangeEventListener) context;
        }

        if(context instanceof TitleChangeListener) {
            titleChangeListener = (TitleChangeListener) context;
        }

        if(!(context instanceof OnFragmentChangeEventListener) || !(context instanceof TitleChangeListener)) {
            throw new RuntimeException(context + " must implement required interfaces");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resourcesUtils = new ResourcesUtils(requireContext());

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(goBackTarget != null) requestFragmentChange(goBackTarget);
                else requireActivity().finish();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (titleChangeListener != null) {
            titleChangeListener.onTitleChanged(getTitle(), goBackTarget);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentChangeListener = null;
    }

    protected void requestFragmentChange(BaseFragment fragment) {
        if (fragmentChangeListener != null) {
            fragmentChangeListener.OnFragmentChangeRequest(fragment);
        }
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public BaseFragment getGoBackTarget(){
        return goBackTarget;
    }
    public void setGoBackTarget(BaseFragment f){
        goBackTarget = f;
    }
}
