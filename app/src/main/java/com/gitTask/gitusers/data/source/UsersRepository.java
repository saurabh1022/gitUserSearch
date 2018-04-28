package com.gitTask.gitusers.data.source;

import android.support.annotation.NonNull;

import com.gitTask.gitusers.data.GitUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rishi on 1/1/2018.
 */

public class UsersRepository implements UsersDataSource {

    private static UsersRepository INSTANCE = null;

    private final UsersDataSource mUsersRemoteDataSource;

    private final UsersDataSource mUsersLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, GitUser> mCacheUsers;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    // left true on purpose because local data source is not implemented yet
    // boolean mCacheIsDirty = true;

    //Todo: add null checks
    // Prevent direct instantiation.
    private UsersRepository(@NonNull UsersDataSource tasksRemoteDataSource,
                            @NonNull UsersDataSource tasksLocalDataSource) {
        mUsersRemoteDataSource = tasksRemoteDataSource;
        mUsersLocalDataSource = tasksLocalDataSource;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param usersRemoteDataSource the backend data source
     * @param usersLocalDataSource  the device storage data source
     * @return the {@link UsersRepository} instance
     */
    public static UsersRepository getInstance(UsersDataSource usersRemoteDataSource,
                                              UsersDataSource usersLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new UsersRepository(usersRemoteDataSource, usersLocalDataSource);
        }
        return INSTANCE;
    }


    /**
     * Gets users from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     */
    @Override
    public void getUsers(@NonNull final LoadUsersCallback callback, boolean forceUpdate) {
        //Todo: add null check for callback
        //Todo: implement cache and local data resource properly in future
        // Respond immediately with cache if available and not dirty
//        if (mCacheUsers != null && !mCacheIsDirty) {
//            callback.onUsersLoaded(new ArrayList<>(mCacheUsers.values()),true);
//            return;
//        }

//        if (mCacheIsDirty || forceUpdate) {
        // If the cache is dirty we need to fetch new data from the network.
        getUsersFromRemoteDataSource(callback, forceUpdate);
//        } else {
        // Query the local storage if available. If not, query the network.
//            mUsersLocalDataSource.getUsers(new LoadUsersCallback() {
//                @Override
//                public void onUsersLoaded(List<GitUser> users, boolean replaceAll) {
//                    refreshCache(users);
//                    callback.onUsersLoaded(new ArrayList<>(mCacheUsers.values()), replaceAll);
//                }
//
//                @Override
//                public void onDataNotAvailable() {
//                    getUsersFromRemoteDataSource(callback);
//                }
//            });
//        }
    }


    //TOdo : check data in cache and local storage in future
    @Override
    public void getMoreUsers(@NonNull final LoadUsersCallback callback) {
        mUsersRemoteDataSource.getMoreUsers(new LoadUsersCallback() {
            @Override
            public void onUsersLoaded(List<GitUser> users) {
                refreshCache(users);
                refreshLocalDataSource(users);
                callback.onUsersLoaded(new ArrayList<>(mCacheUsers.values()));
            }

            @Override
            public void onDataNotAvailable(String message) {
                callback.onDataNotAvailable(message);
            }
        });
    }


    //Todo: check data in cache and local storage in future
    @Override
    public void searchUsers(String searchTerm, final LoadUsersCallback callback) {
        mUsersRemoteDataSource.searchUsers(searchTerm, new LoadUsersCallback() {
            @Override
            public void onUsersLoaded(List<GitUser> users) {
                refreshCache(users);
                refreshLocalDataSource(users);
                callback.onUsersLoaded(new ArrayList<>(mCacheUsers.values()));
            }

            @Override
            public void onDataNotAvailable(String message) {
                callback.onDataNotAvailable(message);
            }
        });
    }


    private void refreshCache(List<GitUser> users) {
        if (mCacheUsers == null) {
            mCacheUsers = new LinkedHashMap<>();
        }
        mCacheUsers.clear();
        for (GitUser user : users) {
            mCacheUsers.put(user.getId(), user);
        }
//        mCacheIsDirty = false;
    }


    private void getUsersFromRemoteDataSource(@NonNull final LoadUsersCallback callback, boolean forceUpdate) {
        mUsersRemoteDataSource.getUsers(new LoadUsersCallback() {
            @Override
            public void onUsersLoaded(List<GitUser> users) {
                refreshCache(users);
                refreshLocalDataSource(users);
                callback.onUsersLoaded(new ArrayList<>(mCacheUsers.values()));
            }

            @Override
            public void onDataNotAvailable(String message) {
                callback.onDataNotAvailable(message);
            }
        }, forceUpdate);
    }

    //Todo: finish refreshlocaldatasorce after cache and local data source feature
    private void refreshLocalDataSource(List<GitUser> users) {
//        mUsersLocalDataSource.deleteAllUsers();
//        for (GitUser user : users) {
//            mUsersLocalDataSource.saveUser(user);
//        }
    }

}
