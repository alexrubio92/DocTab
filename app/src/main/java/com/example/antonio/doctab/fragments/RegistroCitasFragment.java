package com.example.antonio.doctab.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.antonio.doctab.R;
import com.example.antonio.doctab.Utils.Constants;
import com.example.antonio.doctab.fragments.interfaces.MainRegisterInterface;
import com.example.antonio.doctab.helpers.DecodeExtraHelper;
import com.example.antonio.doctab.models.Usuarios;

/**
 * Created by Ricardo on 13/02/2018.
 */

public class RegistroCitasFragment extends Fragment {

    private MainRegisterInterface activityInterface;

    private static DecodeExtraHelper _MAIN_DECODE;
    private static Usuarios _SESSION_USER;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro_citas, container, false);

        _MAIN_DECODE = (DecodeExtraHelper) getActivity().getIntent().getExtras().getSerializable(Constants.KEY_MAIN_DECODE);
        _SESSION_USER = (Usuarios) getActivity().getIntent().getExtras().getSerializable(Constants.KEY_SESSION_USER);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction mainFragment = fragmentManager.beginTransaction();

        mainFragment.replace(R.id.fragment_registro_citas_doctor_container, new FormularioCitasFragment(), Constants.FORMULARIO_CITAS_FRAGMENT);


        mainFragment.replace(R.id.fragment_acciones_citas_doctor_container, new AccionesCitasFragment(), Constants.FORMULARIO_ACCIONES_CITAS_FRAGMENT);
        mainFragment.commit();
        return view;

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activityInterface=(MainRegisterInterface)getActivity();
        }catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString()+"debe implementar");
        }
    }




}