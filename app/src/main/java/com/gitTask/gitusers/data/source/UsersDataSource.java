package com.gitTask.gitusers.data.source;

import android.support.annotation.NonNull;

import com.gitTask.gitusers.data.GitUser;

import java.util.List;

/**
 * Created by rishi on 1/1/2018.
 */

public interface UsersDataSource {

    interface LoadUsersCallback {

        void onUsersLoaded(List<GitUser> users);

        void onDataNotAvailable(String message);
    }

    void getUsers(@NonNull LoadUsersCallback callback, boolean forceUpdate);

    void getMoreUsers(@NonNull LoadUsersCallback callback);

    void searchUsers(String searchTerm, LoadUsersCallback callback);


}
