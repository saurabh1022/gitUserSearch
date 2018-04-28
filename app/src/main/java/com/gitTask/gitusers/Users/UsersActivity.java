package com.gitTask.gitusers.Users;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.gitTask.gitusers.Injection;
import com.gitTask.gitusers.R;

public class UsersActivity extends AppCompatActivity {

    private UsersPresenter mUsersPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserFragment usersFragment =
                (UserFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (usersFragment == null) {
            // Create the fragment
            usersFragment = UserFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, usersFragment);
            transaction.commit();
        }

        // Create the presenter
        mUsersPresenter = new UsersPresenter(Injection.provideUsersRepository(getApplicationContext()), usersFragment);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }
}
