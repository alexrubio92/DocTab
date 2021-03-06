package com.example.antonio.doctab;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.antonio.doctab.Utils.Constants;
import com.example.antonio.doctab.Utils.DateTimeUtils;
import com.example.antonio.doctab.fragments.interfaces.NavigationDrawerInterface;
import com.example.antonio.doctab.helpers.DecodeExtraHelper;
import com.example.antonio.doctab.helpers.DecodeItemHelper;
import com.example.antonio.doctab.models.Citas;
import com.example.antonio.doctab.models.Consultorios;
import com.example.antonio.doctab.models.Doctores;
import com.example.antonio.doctab.models.HorariosDeAtencion;
import com.example.antonio.doctab.models.Pacientes;
import com.example.antonio.doctab.models.Usuarios;
import com.example.antonio.doctab.services.SharedPreferencesService;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NavigationDrawerInterface
        , DialogInterface.OnClickListener {

    private static final String TAG = NavigationDrawerActivity.class.getSimpleName();

    private static Usuarios _SESSION_USER;

    private static DecodeItemHelper _decodeItem;
    private ProgressDialog pDialog;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /**Carga los persmisos de la session**/
        _SESSION_USER = SharedPreferencesService.getUsuarioActual(getApplicationContext());

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onPreRenderMenu(navigationView);
    }

    private void onPreRenderMenu(NavigationView navigationView) {
        onPreRenderSessionMenu(navigationView.getMenu());
    }

    private void onPreRenderSessionMenu(Menu menu) {

        menu.findItem(R.id.menu_title_admin).setVisible(false);
        menu.findItem(R.id.menu_title_doctor).setVisible(false);
        menu.findItem(R.id.menu_title_paciente).setVisible(false);

        switch (_SESSION_USER.getTipoDeUsuario()) {
            case Constants.FB_KEY_ITEM_TIPO_USUARIO_ADMINISTRADOR:
                menu.findItem(R.id.menu_title_admin).setVisible(true);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
                break;
            case Constants.FB_KEY_ITEM_TIPO_USUARIO_DOCTOR:
                menu.findItem(R.id.menu_title_doctor).setVisible(true);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
                break;
            case Constants.FB_KEY_ITEM_TIPO_USUARIO_PACIENTE:
                menu.findItem(R.id.menu_title_paciente).setVisible(true);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
                break;
            default:
                /**Cierra session en caso de no tener usuario**/
                closeNavigation();
                break;
        }

        Log.i(TAG, "TIPO DE USUARIO: " + _SESSION_USER.getTipoDeUsuario());
    }

    @Override
    protected void onStart() {
        super.onStart();
        onPreRender();
    }

    private void onPreRender() {
        onpreRenderHeaderData();
    }

    private void onpreRenderHeaderData() {
        final Usuarios usuario = SharedPreferencesService.getUsuarioActual(getApplicationContext());

        DatabaseReference dbUsuario =
                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.NODOS_USUARIOS_PRIVILEGIOS.get(usuario.getTipoDeUsuario()))
                        .child(usuario.getFirebaseId())
                        .child(usuario.getTipoDeUsuario())
                        .child(usuario.getFirebaseId());

        dbUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                View header = navigationView.getHeaderView(0);
                TextView txtNombrePerfil = (TextView) header.findViewById(R.id.txt_nombre_perfil_header_navigation);
                TextView txtEmail = (TextView) header.findViewById(R.id.txt_email_perfil_header_navigation);

                switch (usuario.getTipoDeUsuario()) {
                    case Constants.FB_KEY_ITEM_DOCTOR:
                        Doctores doctor = dataSnapshot.getValue(Doctores.class);
                        txtNombrePerfil.setText(doctor.getNombreCompleto());
                        txtEmail.setText(doctor.getCorreoElectronico());
                        break;
                    case Constants.FB_KEY_ITEM_PACIENTE:
                        Pacientes paciente = dataSnapshot.getValue(Pacientes.class);
                        txtNombrePerfil.setText(paciente.getNombreCompleto());
                        txtEmail.setText(paciente.getCorreoElectronico());
                        Usuarios user = SharedPreferencesService.getUsuarioActual(getApplicationContext());
                        user.setNombre(paciente.getNombreCompleto());

                        break;
                    case Constants.FB_KEY_ITEM_ADMINISTRADOR:
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                closeNavigation();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeNavigation() {
        /**Cierra la session actual*/
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(NavigationDrawerActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        this.closeAllFragment();

        switch (id) {
            case R.id.menu_item_inicio:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_inicio));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            /**
             * Menu del Doctor
             */
            case R.id.menu_item_citas:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_citas_doctor));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            case R.id.menu_item_consultorios_doctor:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_consultorios_doctor));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            case R.id.menu_item_horarios_de_atencion:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_horarios_servicio));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            case R.id.menu_item_pacientes_doctor:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_pacientes_doctor));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            case R.id.menu_item_perfil_doctor:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_perfil_doctor));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            /**
             * Menu del Paciente
             */

            case R.id.menu_item_citas_paciente:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_citas_doctor));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            case R.id.menu_item_mi_doctor:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_perfil_de_doctor));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            case R.id.menu_item_perfil_paciente:
                getSupportActionBar().setTitle(getString(R.string.default_item_menu_title_perfil_paciente));
                this.openFragment(Constants.ITEM_FRAGMENT.get(id));
                break;
            /**
             * Menu del Administrador
             */
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openFragment(String tag) {
        try {
            FragmentTransaction mainFragment = getSupportFragmentManager().beginTransaction();
            mainFragment.replace(R.id.fragment_main_container, Constants.TAG_FRAGMENT.get(tag), tag);
            mainFragment.addToBackStack(tag);
            mainFragment.commit();
        } catch (Exception e) {
            Toast.makeText(this, "Debe implementrar + " + tag, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cierra todos los fragmentos actuales
     **/
    private void closeAllFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_main_container);
        if (null != fragment)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }


    @Override
    public void setDecodeItem(DecodeItemHelper decodeItem) {
        _decodeItem = decodeItem;
    }

    @Override
    public void openExternalActivity(int action, Class<?> externalActivity) {
        DecodeExtraHelper extraParams = new DecodeExtraHelper();

        extraParams.setTituloActividad(getString(Constants.TITLE_ACTIVITY.get(_decodeItem.getIdView())));
        extraParams.setTituloFormulario(getString(Constants.TITLE_FORM_ACTION.get(action)));
        extraParams.setAccionFragmento(action);
        extraParams.setFragmentTag(Constants.ITEM_FRAGMENT.get(_decodeItem.getIdView()));
        extraParams.setDecodeItem(_decodeItem);

        Intent intent = new Intent(this, externalActivity);
        intent.putExtra(Constants.KEY_MAIN_DECODE, extraParams);
        intent.putExtra(Constants.KEY_SESSION_USER, SharedPreferencesService.getUsuarioActual(getApplicationContext()));
        startActivity(intent);
    }

    @Override
    public void showProgressDialog() {

    }

    @Override
    public void stopProgressDialog() {

    }

    @Override
    public void showQuestion(String titulo, String mensage) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);

        ad.setTitle(titulo);
        ad.setMessage(mensage);
        ad.setCancelable(false);
        ad.setNegativeButton("Cancelar", this);
        ad.setPositiveButton("Aceptar", this);
        ad.show().getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.bootstrap_brand_danger));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        int operation = 0;

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                switch (_decodeItem.getIdView()) {
                    case R.id.item_btn_eliminar_consultorios:
                        operation = Constants.WS_KEY_ELIMINAR_CONSULTORIOS;
                        break;
                    case R.id.item_btn_eliminar_dia:
                        operation = Constants.WS_KEY_ELIMINAR_DIAS;
                        break;
                    case R.id.item_btn_eliminar_citas:
                        operation = Constants.WS_KEY_ELIMINAR_CITAS;
                        break;
                }

                this.webServiceOperations(operation);
        }
    }

    private void webServiceOperations(int operation) {
        pDialog = new ProgressDialog(NavigationDrawerActivity.this);
        pDialog.setMessage(getString(R.string.default_loading_msg));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        switch (operation) {
            case Constants.WS_KEY_ELIMINAR_CONSULTORIOS:
                this.webServiceDeleteConsultorio();
                break;
            case Constants.WS_KEY_ELIMINAR_DIAS:
                this.webServiceDeleteDias();
                break;
            case Constants.WS_KEY_ELIMINAR_CITAS:
                this.webServiceDeleteCitas();
                break;
        }
    }

    private void webServiceDeleteDias() {

        final HorariosDeAtencion dia = (HorariosDeAtencion) _decodeItem.getItemModel();

        /**obtiene la instancia del elemento**/
        DatabaseReference dbdia =
                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FB_KEY_MAIN_DOCTORES)
                        .child(Constants.USUARIO_DOCTOR)
                        .child(Constants.FB_KEY_ITEM_HORARIOS_DE_ATENCION)
                        .child(dia.getFireBaseId());

        dia.setEstatus(Constants.FB_KEY_ITEM_ESTATUS_ELIMINADO);
        dia.setFechaDeEdicion(DateTimeUtils.getTimeStamp());

        dbdia.setValue(dia, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                pDialog.dismiss();
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(),
                            "Eliminado correctamente...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void webServiceDeleteConsultorio() {

        final Consultorios consultorio = (Consultorios) _decodeItem.getItemModel();

        /**obtiene la instancia del elemento**/
        DatabaseReference dbConsultorio =
                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FB_KEY_MAIN_DOCTORES)
                        .child(consultorio.getFireBaseIdDoctor())
                        .child(Constants.FB_KEY_ITEM_CONSULTORIOS)
                        .child(consultorio.getFireBaseId());

        consultorio.setEstatus(Constants.FB_KEY_ITEM_ESTATUS_ELIMINADO);
        consultorio.setFechaDeEdicion(DateTimeUtils.getTimeStamp());

        dbConsultorio.setValue(consultorio, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                pDialog.dismiss();
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(),
                            "Eliminado correctamente...", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void webServiceDeleteCitas() {

        final Citas cita = (Citas) _decodeItem.getItemModel();

        /**obtiene la instancia del elemento**/
        DatabaseReference dbcita =
                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FB_KEY_MAIN_CITAS)
                        .child(cita.getFirebaseIdPaciente())
                        .child(cita.getFireBaseId());

        cita.setEstatus(Constants.FB_KEY_ITEM_ESTATUS_ELIMINADO);
        cita.setFechaDeEdicion(DateTimeUtils.getTimeStamp());

        dbcita.setValue(cita, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                pDialog.dismiss();
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(),
                            "Eliminado correctamente...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
