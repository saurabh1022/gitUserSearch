package com.gitTask.gitusers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gitTask.gitusers.data.source.UsersRepository;
import com.gitTask.gitusers.data.source.remote.UsersRemoteDataSource;

/**
 * Created by Saurabh on 28/04/2018.
 */

public class Injection {

    public static UsersRepository provideUsersRepository(@NonNull Context context) {
        //TOdo: add null check for context
//        ToDoDatabase database = ToDoDatabase.getInstance(context);
        return UsersRepository.getInstance(UsersRemoteDataSource.getInstance(),
               null);
    }
}
