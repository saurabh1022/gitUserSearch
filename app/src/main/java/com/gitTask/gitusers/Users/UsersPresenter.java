package com.gitTask.gitusers.Users;

/**
 * Created by Saurabh on 28/04/2018.
 */

import com.gitTask.gitusers.data.GitUser;
import com.gitTask.gitusers.data.source.UsersDataSource;
import com.gitTask.gitusers.data.source.UsersRepository;

import java.util.List;

/**
 * Listens to user actions from the UI ({@link UserFragment}), retrieves the data and updates the
 * UI as required.
 */
public class UsersPresenter implements UserContract.Presenter {

    private final UsersRepository mUsersRepository;

    private final UserContract.View mUsersView;


    public UsersPresenter(UsersRepository usersRepository, UserContract.View usersView) {
        mUsersRepository = usersRepository;
        mUsersView = usersView;
        mUsersView.setPresenter(this);
    }

    @Override
    public void start() {
        loadUsers(false, true);
    }

    @Override
    public void result(int requestCode, int resultCode) {

    }

    @Override
    public void loadUsers(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mUsersView.setLoadingIndicator(true);
        }
        mUsersRepository.getUsers(new UsersDataSource.LoadUsersCallback() {
            @Override
            public void onUsersLoaded(List<GitUser> users) {
                if (showLoadingUI) {
                    mUsersView.setLoadingIndicator(false);
                }
                mUsersView.showNewUsers(users);
            }

            @Override
            public void onDataNotAvailable(String message) {
                if (showLoadingUI) {
                    mUsersView.setLoadingIndicator(false);
                }
                mUsersView.dataNotAvailable(message);
            }
        }, forceUpdate);
    }

    @Override
    public void loadMoreUsers() {
        mUsersRepository.getMoreUsers(new UsersDataSource.LoadUsersCallback() {
            @Override
            public void onUsersLoaded(List<GitUser> users) {
                mUsersView.showMoreUsers(users);
            }

            @Override
            public void onDataNotAvailable(String message) {
                mUsersView.noMoreUsers();
            }
        });
    }

    @Override
    public void search(String searchTerm) {
        if (searchTerm.equals("")) {
            loadUsers(true, true);
            return;
        }
        mUsersView.setLoadingIndicator(true);
        mUsersRepository.searchUsers(searchTerm, new UsersDataSource.LoadUsersCallback() {
            @Override
            public void onUsersLoaded(List<GitUser> users) {
                mUsersView.setLoadingIndicator(false);
                mUsersView.showNewUsers(users);
            }

            @Override
            public void onDataNotAvailable(String message) {
                mUsersView.setLoadingIndicator(false);
                mUsersView.noMoreUsers();
            }
        });
    }
}
